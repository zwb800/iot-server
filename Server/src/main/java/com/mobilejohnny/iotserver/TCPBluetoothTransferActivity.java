package com.mobilejohnny.iotserver;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.*;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.xiaomi.mipush.sdk.MiPushClient;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by admin2 on 2015/3/20.
 */
public class TCPBluetoothTransferActivity extends ActionBarActivity {
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private Bluetooth bluetooth;
    private TCP tcp;
    private UDP udp;
    private ConnectionListener connectionListener;
    private PowerManager.WakeLock wakeLock;

    private int port = 8080;
    private String bluetoothDeviceName ="OFFICE";
    private TextView txtIP;
    private Receiver receiver;
    private String connection_type = CONNECT_UDP;
    private static final String CONNECT_TCP = "TCP";
    private static final String CONNECT_UDP = "UDP";
    private TextView txtRX;
    private TextView txtTX;

    private UsbManager usbManager;
    private FDTI fdti;
    private String dest_type = DEST_BLUETOOTH;
    private static final String DEST_BLUETOOTH = "Bluetooth";
    private static final String DEST_USBOTG = "USB-OTG";
    private TextView txtDestType;
    private TextView txtConnectType;
    private TextView txtStatus;
    private ConnectThread fromThread;
    private ConnectThread destThread;
    private ConnectThread.ConnectThreadListener connectRxThreadListener;
    private ConnectThread.ConnectThreadListener connectTxThreadListener;
    private LocationManager locationManager;
    private MSP msp;
    private int fixSatelliteCount;
    private int satelliteCount;
    private UDP.UDPListener fromListener;
    private LocationListener locationListener;
    private GpsStatus.Listener gpsListener;
    private TextView txtSatellite;
    private TextView txtLongitude;
    private TextView txtLatitude;
    private TextView txtAltitude;
    private TextView txtSpeed;
    private TextView txtAccuracy;
    private boolean enableGPS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpbluetoothtransfer);
        readPreference();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        msp = new MSP();

        wakeLock =  powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"TCP_BT");

        txtRX = (TextView)findViewById(R.id.txt_rx);
        txtTX = (TextView)findViewById(R.id.txt_tx);
        txtConnectType = (TextView)findViewById(R.id.txt_connection_type);
        txtDestType = (TextView)findViewById(R.id.txt_dest_type);
        txtStatus = (TextView) findViewById(R.id.txt_status);
        txtIP = (TextView)findViewById(R.id.txt_ip);
        txtSatellite = (TextView)findViewById(R.id.txt_satellite);
        txtLongitude = (TextView)findViewById(R.id.txt_longitude);
        txtLatitude = (TextView)findViewById(R.id.txt_latitude);
        txtAltitude = (TextView)findViewById(R.id.txt_altitude);
        txtSpeed = (TextView)findViewById(R.id.txt_speed);
        txtAccuracy = (TextView)findViewById(R.id.txt_accuracy);

        txtConnectType.setText(connection_type);
        txtDestType.setText(dest_type);

        int ip = wifiManager.getDhcpInfo().ipAddress;
        txtIP.setText(getIPString(ip)+":"+port);

        registerReceiver();

        final Handler handler =  new Handler();

       connectTxThreadListener = new ConnectThread.ConnectThreadListener() {
            @Override
            public void onReceive(final byte[] data) {
                final String str = convertToString(data);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtTX.setText(str);
                    }
                });
            }
        };

        connectRxThreadListener = new ConnectThread.ConnectThreadListener() {
            @Override
            public void onReceive(final byte[] data) {
                final String str = convertToString(data);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtRX.setText(str);
                    }
                });
            }
        };

        connectionListener = new ConnectionListener(){

            @Override
            public void result(final int result,InputStream inputStream,OutputStream outputStream) {

                if(result==ConnectionListener.RESULT_SUCCESS){
                    TCPBluetoothTransferActivity.inputStream = inputStream;
                    TCPBluetoothTransferActivity.outputStream = outputStream;
                    if(connection_type.equals(CONNECT_TCP))
                    {
                        tcp.startServer(port,fromListener);
                    }
                    else if(connection_type.equals(CONNECT_UDP))
                    {
                        udp.startServer(port,fromListener);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String msg = result == ConnectionListener.RESULT_SUCCESS ? "已连接" : "连接失败";

                        if (dest_type == DEST_BLUETOOTH)
                            msg = (bluetoothDeviceName + " " + msg);

                        txtStatus.setText(msg);
                    }
                });

            }
        };

        fromListener = new UDP.UDPListener(){
            @Override
            public void onConnected(InputStream inputStream, OutputStream outputStream) {
            fromThread = new ConnectThread(inputStream, TCPBluetoothTransferActivity.outputStream);
            fromThread.setListener(connectRxThreadListener);
            destThread =  new ConnectThread(TCPBluetoothTransferActivity.inputStream,outputStream);
            destThread.setListener(connectTxThreadListener);
            fromThread.start();
            destThread.start();
            }
        };

        if(connection_type.equals(CONNECT_TCP))
        {
            tcp = new TCP();
        }
        else
        {
            udp = new UDP();
        }

        if(dest_type.equals( DEST_BLUETOOTH))
        {
            bluetooth = new Bluetooth(this,connectionListener);
        }
        else
        {
            fdti = new FDTI(usbManager);
            fdti.setListener(connectionListener);
        }

        requestLocation();

        connect();

        wakeLock.acquire();
    }

    private void requestLocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateGPS(location.getLongitude(),
                        location.getLatitude(),
                        location.getAltitude(),
                        location.getSpeed(),
                        location.getAccuracy());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(enableGPS)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            gpsListener = new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int i) {
                    GpsStatus status = locationManager.getGpsStatus(null);
                    if (status != null) {
                        Iterator<GpsSatellite> iterator = status.getSatellites().iterator();
                        fixSatelliteCount = 0;
                        satelliteCount = 0;
                        while (iterator.hasNext()) {
                            GpsSatellite satellite = iterator.next();
                            if (satellite.usedInFix()) {
                                fixSatelliteCount++;
                            }
                            satelliteCount++;
                        }
                    }
                }
            };
            locationManager.addGpsStatusListener(gpsListener);
        }
    }

    private void updateGPS(double longitude, double latitude, double altitude, float speed,float accuracy) {
        msp.updateGPS(fixSatelliteCount,
                satelliteCount,
                (long)(longitude*10000000),
                (long)(latitude*10000000),
                (int)altitude,
                (int)(speed*100));
        if(fromThread!=null)
        {
            fromThread.setAnsyData(msp.getMSP_GPS());//将gps数据附加到遥控数据后
        }

        txtSatellite.setText(fixSatelliteCount+"/"+satelliteCount);
        txtLongitude.setText(longitude+"");
        txtLatitude.setText(latitude+"");
        txtAltitude.setText(altitude+"");
        txtSpeed.setText(speed+"");
        txtAccuracy.setText(accuracy+"");
    }

    private void connect() {
        if(dest_type.equals( DEST_BLUETOOTH))
        {
            bluetooth.connect(bluetoothDeviceName);
        }
        else
        {
            connectUsb();
        }

        txtStatus.setText("正在连接...");
    }

    private void readPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        connection_type = preferences.getString("connection_type",CONNECT_TCP);
        dest_type = preferences.getString("destination_type",DEST_BLUETOOTH);
        port = Integer.parseInt(preferences.getString("port", "0"));
        bluetoothDeviceName = preferences.getString("device_name","BTCOM");

        enableGPS = preferences.getBoolean(getString(R.string.key_enable_gps), false);
    }

    private void connectUsb()
    {
        HashMap<String, UsbDevice> deviceSet =  usbManager.getDeviceList();
        Log.i(getClass().getSimpleName(),"开始检测USB设备");
        for (UsbDevice device:deviceSet.values())
        {

            if(usbManager.hasPermission(device))
            {
                Log.i("USB-OTG","开始连接"+device.getDeviceName());
                fdti.begin(device);
            }
            else
            {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,new Intent(UsbHostActivity.USB_PERMISSION),0);
                usbManager.requestPermission(device,pendingIntent);
            }
        }
    }

    private void registerReceiver() {
        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(UsbHostActivity.USB_PERMISSION);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);

        registerReceiver(receiver, intentFilter);
    }

    private String convertToString(byte[] data) {
        StringBuffer stringBuffer = new StringBuffer(data.length*2);
        for (int i=0;i<data.length;i++)
        {

            stringBuffer.append( String.format("%x ",data[i]).toUpperCase());

        }
        return stringBuffer.toString();
    }

    private String getIPString(int ip) {
        return String.format(
                    "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);

        if(tcp!=null)
            tcp.close();
        if(udp!=null)
            udp.close();
        if(bluetooth!=null)
            bluetooth.close();

        if(enableGPS)
        {
            locationManager.removeUpdates(locationListener);
            locationManager.removeGpsStatusListener(gpsListener);
        }

        wakeLock.release();

        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent("com.mobilejohnny.iotserver.action.Settings"),0);
            return true;
        }
        else if (id == R.id.action_exit) {
            BluetoothService.startActionDisconnect(this);
            MiPushClient.unregisterPush(this);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
            {

            }
        }
    }

    public class Receiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)==
                        BluetoothAdapter.STATE_ON)
                connect();
            }
            else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
            {

            }
            else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            {

            }
            else if(intent.getAction().equals(UsbHostActivity.USB_PERMISSION))
            {
                if(dest_type.equals(DEST_USBOTG))
                {
                    synchronized (this)
                    {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false))
                        {
                            if(device!=null)
                            {
                                connectUsb();
                            }
                        }
                        else
                        {
                            Log.i(this.getClass().getSimpleName(),"Permission Denied");
                        }
                    }
                }
            }
        }
    }


}
