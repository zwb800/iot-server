package com.mobilejohnny.iotwidget;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.params.BlackLevelPattern;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BluetoothService extends Service {

    private static final String ACTION_CONNECT = "com.mobilejohnny.iotwidget.action.CONNECT";
    private static final String ACTION_DISCONNECT =  "com.mobilejohnny.iotwidget.action.DISCONNECT";

    private static final String ACTION_SEND = "com.mobilejohnny.iotwidget.action.SEND";

    public static final String EXTRA_MESSAGE = "com.mobilejohnny.iotwidget.extra.PARAM_MESSAGE";
    public static final String ACTION_CONNECTION_STATE_CHANGE = "com.mobilejohnny.iotwidget.action.CONNECTION_STATE_CHANGE";
    public static final String ACTION_CONNECTION_SEND_STATE = "com.mobilejohnny.iotwidget.action.CONNECTION_SEND_STATE";

    public static final String EXTRA_STATE = "com.mobilejohnny.iotwidget.extra.STATE";

    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECT_FAILD = 3;
    public static final int STATE_DISCONNECTED = 4;
    private static final int MAX_RECONNECT = 0;
    private static final String EXTRA_DELAY = "com.mobilejohnny.iotwidget.extra.DELAY";

    private Bluetooth bluetooth;

    private String bluetoothDeviceName ="OFFICE";//蓝牙设备名

    private static int reconnectcount;

    public static void startActionSend(Context context,int appWidgetID, byte[] msg) {
        Intent intent = new Intent(context, BluetoothService.class);
        intent.putExtra(ButtonAppWidget.EXTRA_APPWIDGETID,appWidgetID);
        intent.putExtra(EXTRA_MESSAGE, msg);
        intent.setAction(ACTION_SEND);
        context.startService(intent);
    }

    public static void startActionConnect(Context context,int delay) {
        Intent intent = new Intent(context, BluetoothService.class);
        intent.setAction(ACTION_CONNECT);
        intent.putExtra(EXTRA_DELAY,delay);
        context.startService(intent);
    }

    public static void startActionReconnect(Context context)
    {
        if(reconnectcount<MAX_RECONNECT)
        {
           int delay = 1;
            for (int i=0;i<reconnectcount;i++)
            {
                delay *= 2;
            }
            delay = Math.min( delay*3000,500000);//最大重连间隔5分钟

            startActionConnect(context,delay);//三秒后重试
            reconnectcount++;
        }
        else
        {
            Log.i("ConnectionService","重试次数已到");
        }
    }

    public static void startActionDisconnect(Context context) {

        Intent intent = new Intent(context, BluetoothService.class);
        intent.setAction(ACTION_DISCONNECT);
        context.startService(intent);
    }

    public BluetoothService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        readPreference();

       bluetooth = new Bluetooth(null, new ConnectionListener() {
           @Override
           public void result(int result, InputStream in, OutputStream out) {
               
               if(result == ConnectionListener.RESULT_SUCCESS)
               {
                   connectStateChange(getBaseContext(),STATE_CONNECTED);
                   reconnectcount = 0;
               }
               else if(result == ConnectionListener.RESULT_FAILD)
               {
                   connectStateChange(getBaseContext(),STATE_CONNECT_FAILD);
               }
           }
       });

    }


    private void readPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        bluetoothDeviceName = preferences.getString("device_name","BTCOM");
    }

    public static void connectStateChange(Context context,int state) {
        Intent i = new Intent(ACTION_CONNECTION_STATE_CHANGE);
        i.putExtra(EXTRA_STATE,state);
        context.sendBroadcast(i);
    }


    @Override
    public void onDestroy() {
        handleActionDisconnect();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_SEND.equals(action)) {
                handleActionSend(intent);
            }
            else if (ACTION_CONNECT.equals(action)) {
                int delay = intent.getIntExtra(EXTRA_DELAY,0);
                handleActionConnect(delay);
            }
            else if (ACTION_DISCONNECT.equals(action)) {
                handleActionDisconnect();
            }
        }

        return Service.START_STICKY;
    }

    private void handleActionDisconnect() {

        if(bluetooth!=null)
            bluetooth.close();

        stopSelf();
    }

    private void handleActionConnect(int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }, delay);
        Log.i(getClass().getSimpleName(),"延迟后连接 "+delay);
    }

    private void handleActionSend(Intent intent) {

        byte[] data = intent.getByteArrayExtra(EXTRA_MESSAGE);
        send(data);
    }

    private void connect() {
        bluetooth.connect(bluetoothDeviceName);
        connectStateChange(getBaseContext(),STATE_CONNECTING);
    }

    private void send(byte[] data) {


        if(bluetooth.send(data))
        {

        }
    }
}
