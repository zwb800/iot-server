package com.mobilejohnny.iotserver;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

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
    private UDP.UDPListener udpListener;
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
    private ConnectThread udpThread;
    private ConnectThread destThread;
    private ConnectThread.ConnectThreadListener connectRxThreadListener;
    private ConnectThread.ConnectThreadListener connectTxThreadListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpbluetoothtransfer);
        readPreference();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);

        wakeLock =  powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"TCP_BT");

        txtRX = (TextView)findViewById(R.id.txt_rx);
        txtTX = (TextView)findViewById(R.id.txt_tx);
        txtConnectType = (TextView)findViewById(R.id.txt_connection_type);
        txtDestType = (TextView)findViewById(R.id.txt_dest_type);
        txtStatus = (TextView) findViewById(R.id.txt_status);
        txtIP = (TextView)findViewById(R.id.txt_ip);

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
                        tcp.startServer(port,udpListener);
                    }
                    else if(connection_type.equals(CONNECT_UDP))
                    {
                        udp.startServer(port,udpListener);
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

        udpListener = new UDP.UDPListener(){
            @Override
            public void onConnected(InputStream inputStream, OutputStream outputStream) {
            udpThread = new ConnectThread(inputStream, TCPBluetoothTransferActivity.outputStream);
            udpThread.setListener(connectRxThreadListener);
            destThread =  new ConnectThread(TCPBluetoothTransferActivity.inputStream,outputStream);
            destThread.setListener(connectTxThreadListener);
            udpThread.start();
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
            bluetooth.connect(bluetoothDeviceName);
        }
        else
        {
            fdti = new FDTI(usbManager);
            fdti.setListener(connectionListener);
            connectUsb();
        }

        wakeLock.acquire();
    }

    private void readPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        connection_type = preferences.getString("connection_type",CONNECT_TCP);
        dest_type = preferences.getString("destination_type",DEST_BLUETOOTH);
        port = Integer.parseInt(preferences.getString("port", "0"));
        bluetoothDeviceName = preferences.getString("device_name","BTCOM");
    }

    private void connectUsb()
    {
        HashMap<String, UsbDevice> deviceSet =  usbManager.getDeviceList();
        Log.i(getClass().getSimpleName(),"开始检测USB设备");
        for (UsbDevice device:deviceSet.values())
        {

            if(usbManager.hasPermission(device))
            {
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
            stringBuffer.append(data[i]);
            stringBuffer.append(" ");
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
            startActivity(new Intent("com.mobilejohnny.iotserver.action.Settings"));
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
                bluetooth.connect(bluetoothDeviceName);
            }
            else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
            {
                if(udpThread!=null)
                udpThread.setListener(null);
                if(destThread!=null)
                destThread.setListener(null);
            }
            else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            {
                if(udpThread!=null)
                udpThread.setListener(connectRxThreadListener);
                if(destThread!=null)
                destThread.setListener(connectTxThreadListener);
            }
            else if(intent.getAction().equals(UsbHostActivity.USB_PERMISSION))
            {
                synchronized (this)
                {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false))
                    {
                        if(device!=null)
                        {
                            fdti.begin(device);
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
