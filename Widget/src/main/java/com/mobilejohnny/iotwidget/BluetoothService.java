package com.mobilejohnny.iotwidget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetProvider;
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
    public static final String EXTRA_DEVICENAME = "com.mobilejohnny.iotwidget.extra.EXTRA_DEVICENAME";
    public static final String EXTRA_APPWIDGETID = "com.mobilejohnny.iotwidget.extra.EXTRA_APPWIDGETID";

    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECT_FAILD = 3;
    public static final int STATE_DISCONNECTED = 4;
    private static final int MAX_RECONNECT = 0;
    private static final String EXTRA_DELAY = "com.mobilejohnny.iotwidget.extra.DELAY";

    private Bluetooth bluetooth;

    private String bluetoothDeviceName ="OFFICE";//蓝牙设备名

    private static int reconnectcount;

    public static void startActionSend(Context context,int appWidgetID,String deviceName, byte[] msg) {
        Intent intent = new Intent(context, BluetoothService.class);
        intent.putExtra(EXTRA_APPWIDGETID,appWidgetID);
        intent.putExtra(EXTRA_DEVICENAME,deviceName);
        intent.putExtra(EXTRA_MESSAGE, msg);
        intent.setAction(ACTION_SEND);
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
        i.putExtra(EXTRA_STATE, state);
        context.sendBroadcast(i);
    }


    @Override
    public void onDestroy() {
        bluetooth.close();
        bluetooth = null;
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
        }

        return Service.START_STICKY;
    }


    private void handleActionSend(Intent intent) {

        final byte[] data = intent.getByteArrayExtra(EXTRA_MESSAGE);
        final String deviceName = intent.getStringExtra(EXTRA_DEVICENAME);
        final int appWidgetID = intent.getIntExtra(EXTRA_APPWIDGETID,-1);
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(!bluetooth.send(deviceName,data))
                {
                    Log.e(BluetoothService.this.getClass().getSimpleName(),"发送失败");
                }

                ButtonAppWidget.startActionClicked(BluetoothService.this,appWidgetID);
                bluetooth.close();
            }
        }).start();


    }
}
