package com.mobilejohnny.iotserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
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
    private TCP.TCPListener tcpListener;
    private View decorView;
    private PowerManager.WakeLock wakeLock;
    private TextView txtBluetooth;
    private TextView txtData;
    private int port = 8080;
    private String bluetoothDeviceName ="BTCOM";
    private TextView txtIP;
    private BluetoothStateReceiver bluetoothStateReceiver;
    private UDP.UDPListener udpListener;
    private int connect_type = CONNECT_UDP;
    private static final int CONNECT_TCP = 1;
    private static final int CONNECT_UDP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpbluetoothtransfer);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);


        wakeLock =  powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"TCP_BT");

        decorView = getWindow().getDecorView();


        txtData = (TextView)findViewById(R.id.txt_data);
        txtBluetooth = (TextView) findViewById(R.id.txt_bluetooth);
        txtIP = (TextView)findViewById(R.id.txt_ip);

        int ip = wifiManager.getDhcpInfo().ipAddress;
        txtIP.setText(getIPString(ip)+":"+port);

        bluetoothStateReceiver = new BluetoothStateReceiver();

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver,intentFilter);

        final Handler bt =  new Handler();

        bluetoothListener = new Bluetooth.BluetoothListener(){

            @Override
            public void result(final int result) {
                final String msg = result == Bluetooth.RESULT_SUCCESS ? "已连接" : "连接失败";
                bt.post(new Runnable() {
                    @Override
                    public void run() {
                        txtBluetooth.setText( bluetoothDeviceName+" "+msg);
                    }
                });
            }

            @Override
            public void onConnected(BluetoothSocket socket) {
                if(connect_type==CONNECT_UDP) {
                    try {
                        ConnectThread tcpThread = new ConnectThread(udp.getInputStream(), socket.getOutputStream());
                        tcpThread.setListener(new ConnectThread.ConnectThreadListener() {
                            @Override
                            public void onReceive(final String data) {
                                bt.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtData.setText(data);
                                    }
                                });
                            }
                        });
                        tcpThread.start();
                        new ConnectThread(socket.getInputStream(), udp.getOutputStream()).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        tcpListener = new TCP.TCPListener() {

            @Override
            public void onConnected(Socket socket) {

                try {
                    ConnectThread tcpThread = new ConnectThread(socket.getInputStream(),bluetooth.getOutputStream());
                    tcpThread.setListener(new ConnectThread.ConnectThreadListener() {
                        @Override
                        public void onReceive(final String data) {
                            bt.post(new Runnable() {
                                @Override
                                public void run() {
                                    txtData.setText(data);
                                }
                            });
                        }
                    });
                    tcpThread.start();
                    new ConnectThread(bluetooth.getInputStream(),socket.getOutputStream()).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        udpListener = new UDP.UDPListener(){
            @Override
            public void onConnected(InputStream inputStream, OutputStream outputStream) {

            }
        };

        bluetooth = new Bluetooth(this,bluetoothListener);
        bluetooth.connect(bluetoothDeviceName);

        tcp = new TCP();
        udp = new UDP();

        if(connect_type == CONNECT_TCP)
        {
            tcp.startServer(port,tcpListener);
        }
        else if(connect_type == CONNECT_UDP)
        {
            udp.startServer(port,udpListener);
        }

        wakeLock.acquire();
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
        tcp.close();
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

}
