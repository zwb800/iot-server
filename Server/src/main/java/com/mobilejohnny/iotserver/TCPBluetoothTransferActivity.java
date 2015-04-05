package com.mobilejohnny.iotserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.*;
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

/**
 * Created by admin2 on 2015/3/20.
 */
public class TCPBluetoothTransferActivity extends ActionBarActivity {
    private Bluetooth bluetooth;
    private TCP tcp;
    private UDP udp;
    private Bluetooth.BluetoothListener bluetoothListener;
    private PowerManager.WakeLock wakeLock;
    private TextView txtBluetooth;
    private int port = 8080;
    private String bluetoothDeviceName ="OFFICE";
    private TextView txtIP;
    private BluetoothStateReceiver bluetoothStateReceiver;
    private UDP.UDPListener udpListener;
    private int connect_type = CONNECT_UDP;
    private static final int CONNECT_TCP = 1;
    private static final int CONNECT_UDP = 2;
    private TextView txtRX;
    private TextView txtTX;
    private boolean show_rxtx = true;
    private ScreenReceiver screenReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpbluetoothtransfer);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        connect_type = Integer.parseInt(preferences.getString("connection_type",CONNECT_TCP+""));
        port = Integer.parseInt(preferences.getString("port","0"));

        bluetoothDeviceName = preferences.getString("device_name","BTCOM");

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        wakeLock =  powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"TCP_BT");

        txtRX = (TextView)findViewById(R.id.txt_rx);
        txtTX = (TextView)findViewById(R.id.txt_tx);
        txtBluetooth = (TextView) findViewById(R.id.txt_bluetooth);
        txtIP = (TextView)findViewById(R.id.txt_ip);

        int ip = wifiManager.getDhcpInfo().ipAddress;
        txtIP.setText(getIPString(ip)+":"+port);

        registerReceiver();

        final Handler handler =  new Handler();

        final ConnectThread.ConnectThreadListener connectTxThreadListener = new ConnectThread.ConnectThreadListener() {
            @Override
            public void onReceive(final byte[] data) {
                if(show_rxtx) {
                    final String str = convertToString(data);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            txtTX.setText(str);
                        }
                    });
                }
            }
        };

        final ConnectThread.ConnectThreadListener connectRxThreadListener = new ConnectThread.ConnectThreadListener() {
            @Override
            public void onReceive(final byte[] data) {
                if(show_rxtx)
                {
                    final String str = convertToString(data);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            txtRX.setText(str);
                        }
                    });
                }

            }
        };

        bluetoothListener = new Bluetooth.BluetoothListener(){

            @Override
            public void result(final int result) {
                final String msg = result == Bluetooth.RESULT_SUCCESS ? "已连接" : "连接失败";
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtBluetooth.setText( bluetoothDeviceName+" "+msg);
                    }
                });
            }

            @Override
            public void onConnected(InputStream inputStream,OutputStream outputStream) {

                if(connect_type == CONNECT_TCP)
                {
                    tcp.startServer(port,udpListener);
                }
                else if(connect_type == CONNECT_UDP)
                {
                    udp.startServer(port,udpListener);
                }
            }
        };

//        tcpListener = new TCP.TCPListener() {
//            @Override
//            public void onConnected(Socket socket) {
//                try {
//                    ConnectThread tcpThread = new ConnectThread(socket.getInputStream(),bluetooth.getOutputStream());
//                    tcpThread.setListener(connectRxThreadListener);
//                    tcpThread.start();
//                    ConnectThread btThread = new ConnectThread(bluetooth.getInputStream(),socket.getOutputStream());
//                    btThread.setListener(connectTxThreadListener);
//                    btThread.start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        };

        udpListener = new UDP.UDPListener(){
            @Override
            public void onConnected(InputStream inputStream, OutputStream outputStream) {
                try {
                    ConnectThread udpThread = new ConnectThread(inputStream, bluetooth.getOutputStream());
                    udpThread.setListener(connectRxThreadListener);
                    udpThread.start();
                    ConnectThread btThread =  new ConnectThread(bluetooth.getInputStream(),outputStream);
                    btThread.setListener(connectTxThreadListener);
                    btThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        tcp = new TCP();
        udp = new UDP();
        bluetooth = new Bluetooth(this,bluetoothListener);
        bluetooth.connect(bluetoothDeviceName);

        wakeLock.acquire();
    }

    private void registerReceiver() {
        bluetoothStateReceiver = new BluetoothStateReceiver();

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, intentFilter);

        screenReceiver = new ScreenReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenReceiver, intentFilter);
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

        unregisterReceiver(bluetoothStateReceiver);
        unregisterReceiver(screenReceiver);

        tcp.close();
        udp.close();
        bluetooth.close();
        wakeLock.release();

        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

    public class BluetoothStateReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)==
                        BluetoothAdapter.STATE_ON)
                bluetooth.connect(bluetoothDeviceName);
            }
        }
    }

    public class ScreenReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
            {
                show_rxtx = false;
            }
            else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            {
                show_rxtx = true;
            }

            Log.i("TCP-BT","show_rxtx:"+show_rxtx);
        }
    }

}
