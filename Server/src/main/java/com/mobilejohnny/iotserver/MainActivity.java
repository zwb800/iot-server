package com.mobilejohnny.iotserver;

import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.mobilejohnny.iotserver.utils.StringUtils;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.*;
import org.w3c.dom.Text;


public class MainActivity extends ActionBarActivity  {

    // user your appid the key.
    public static final String APP_ID = "2882303761517303294";
    // user your appid the key.
    public static final String APP_KEY = "5161730349294";

    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep
    // com.xiaomi.mipushdemo
    public static final String TAG = "xmpush";
    private static final String ACTION_BLUETOOTH_CONNECT_RESULT = "com.mobilejohnny.iotserver.action.BLUETOOTH_CONNECT_RESULT";

    Receiver receiver = null;

    private Handler handler = null;
    private TextView txtRX;
    private TextView txtTX;
    private TextView txtConnectType;
    private TextView txtDestType;
    private TextView txtStatus;
    private TextView txtIP;
    private TextView txtRegID;

    private String dest_type = DEST_BLUETOOTH;
    private static final String DEST_BLUETOOTH = "Bluetooth";
    private static final String DEST_USBOTG = "USB-OTG";

    private String connection_type = CONNECT_UDP;
    private static final String CONNECT_TCP = "TCP";
    private static final String CONNECT_UDP = "UDP";

    private String bluetoothDeviceName ="OFFICE";//蓝牙设备名
    private int port = 8080;//监听端口
    private boolean enableGPS;//发送GPS数据 用于Multiwii


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        readPreference();

        ConnectionService.startActionConnect(this);

        txtRX = (TextView)findViewById(R.id.txt_rx);
        txtTX = (TextView)findViewById(R.id.txt_tx);
        txtConnectType = (TextView)findViewById(R.id.txt_connection_type);
        txtDestType = (TextView)findViewById(R.id.txt_dest_type);
        txtStatus = (TextView) findViewById(R.id.txt_status);
        txtIP = (TextView)findViewById(R.id.txt_ip);
        txtRegID = (TextView)findViewById(R.id.txt_regid);

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

        int ip = wifiManager.getDhcpInfo().ipAddress;
        txtIP.setText(StringUtils.getIPString(ip)+":"+port);
        txtConnectType.setText(connection_type);
        txtDestType.setText(dest_type);

        MiPushClient.registerPush(this, APP_ID, APP_KEY);

        setLogger();
    }

    private void readPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        connection_type = preferences.getString("connection_type",CONNECT_TCP);
        dest_type = preferences.getString("destination_type", DEST_BLUETOOTH);
        port = Integer.parseInt(preferences.getString("port", "0"));
        bluetoothDeviceName = preferences.getString("device_name","BTCOM");

        enableGPS = preferences.getBoolean(getString(R.string.key_enable_gps), false);
    }



    private void setLogger() {
        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };
        Logger.setLogger(this, newLogger);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_exit) {
            ConnectionService.startActionDisconnect(this);
            MiPushClient.unregisterPush(this);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(receiver);

        receiver = null;
        handler = null;
        super.onStop();
    }

    private void registerReceiver() {
        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectionService.ACTION_RX);
        intentFilter.addAction(ConnectionService.ACTION_TX);
        intentFilter.addAction(ConnectionService.ACTION_CONNECTED);
        intentFilter.addAction(ConnectionService.ACTION_XMPUSH_REGISTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(UsbHostActivity.USB_PERMISSION);

        registerReceiver(receiver, intentFilter);
    }

    public static void startActionXMPush_Registed(Context context,String reg_id)
    {
        Intent i = new Intent(ConnectionService.ACTION_XMPUSH_REGISTED);
        i.putExtra(ConnectionService.EXTRA_PARAM_MESSAGE, reg_id);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    public static void startActionBluetoothConnectResult(Context context,int result) {
        Intent i = new Intent(ACTION_BLUETOOTH_CONNECT_RESULT);
        i.putExtra(ConnectionService.EXTRA_PARAM_MESSAGE, result);
        context.sendBroadcast(i);
    }

    private class Receiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(ConnectionService.ACTION_TX)) {
                final String str = StringUtils.convertToString(intent.getByteArrayExtra(ConnectionService.EXTRA_PARAM_MESSAGE));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtTX.setText(str);
                    }
                });
            }
            else if(intent.getAction().equals(ConnectionService.ACTION_RX)) {
                final String str = StringUtils.convertToString(intent.getByteArrayExtra(ConnectionService.EXTRA_PARAM_MESSAGE));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtRX.setText(str);
                    }
                });
            }
            else if(intent.getAction().equals(ConnectionService.ACTION_CONNECTED)) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtStatus.setText(bluetoothDeviceName+" 已连接");
                    }
                });
            }
            else if(intent.getAction().equals(ConnectionService.ACTION_XMPUSH_REGISTED)) {
                final String regid = intent.getStringExtra(ConnectionService.EXTRA_PARAM_MESSAGE);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtRegID.setText(regid);
                    }
                });

            }
            else if(intent.getAction().equals(ACTION_BLUETOOTH_CONNECT_RESULT)) {

            }
        }
    }
}
