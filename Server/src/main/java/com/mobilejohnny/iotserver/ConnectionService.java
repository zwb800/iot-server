package com.mobilejohnny.iotserver;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
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
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ConnectionService extends Service {

    private static final String ACTION_CONNECT = "com.mobilejohnny.iotserver.action.CONNECT";
    private static final String ACTION_DISCONNECT =  "com.mobilejohnny.iotserver.action.DISCONNECT";

    private static final String ACTION_SEND = "com.mobilejohnny.iotserver.action.SEND";

    public static final String ACTION_RX = "com.mobilejohnny.iotserver.action.RX";
    public static final String ACTION_TX = "com.mobilejohnny.iotserver.action.TX";
    public static final String EXTRA_MESSAGE = "com.mobilejohnny.iotserver.extra.PARAM_MESSAGE";
    public static final String ACTION_XMPUSH_REGISTED = "com.mobilejohnny.iotserver.action.XMPUSH_REGISTER";
    public static final String ACTION_CONNECTION_STATE_CHANGE = "com.mobilejohnny.iotserver.action.CONNECTION_STATE_CHANGE";
    public static final String EXTRA_STATE = "com.mobilejohnny.iotserver.extra.STATE";

    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECT_FAILD = 3;
    public static final int STATE_DISCONNECTED = 4;
    private static final int MAX_RECONNECT = 100;
    private static final String EXTRA_DELAY = "com.mobilejohnny.iotserver.extra.DELAY";

    private UsbManager usbManager;
    private LocationManager locationManager;

    private Bluetooth bluetooth;
    private FDTI fdti;
    private TCP tcp;
    private UDP udp;

    private ConnectThread fromThread;
    private ConnectThread destThread;

    private String dest_type = DEST_BLUETOOTH;
    private static final String DEST_BLUETOOTH = "Bluetooth";
    private static final String DEST_USBOTG = "USB-OTG";

    private String connection_type = CONNECT_UDP;
    private static final String CONNECT_TCP = "TCP";
    private static final String CONNECT_UDP = "UDP";

    private String bluetoothDeviceName ="OFFICE";//蓝牙设备名
    private int port = 8080;//监听端口
    private boolean enableGPS;//发送GPS数据 用于Multiwii

    private static int reconnectcount;

    public static void startActionSend(Context context, byte[] msg) {

        Intent intent = new Intent(context, ConnectionService.class);
        intent.putExtra(EXTRA_MESSAGE, msg);
        intent.setAction(ACTION_SEND);
        context.startService(intent);

    }

    public static void startActionConnect(Context context,int delay) {
        Intent intent = new Intent(context, ConnectionService.class);
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

        Intent intent = new Intent(context, ConnectionService.class);
        intent.setAction(ACTION_DISCONNECT);
        context.startService(intent);
    }

    public ConnectionService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        readPreference();
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        ConnectionListener destListener = new ConnectionListener(){
            @Override
            public void result(final int result, final InputStream destinputStream,final OutputStream destoutputStream) {

                ConnectionListener fromListener = new ConnectionListener(){
                    @Override
                    public void result(final int result,InputStream srcinputStream,OutputStream srcoutputStream){

                        ConnectThread.ConnectThreadListener connectTxThreadListener = new ConnectThread.ConnectThreadListener() {
                            @Override
                            public void onReceive(final byte[] data) {
                                tx(data);
                            }
                        };

                        ConnectThread.ConnectThreadListener connectRxThreadListener = new ConnectThread.ConnectThreadListener() {
                            @Override
                            public void onReceive(final byte[] data) {
                                rx(data);
                            }
                        };

                        //源连接建立后建立线程
                        fromThread = new ConnectThread(destinputStream, srcoutputStream);
                        fromThread.setListener(connectRxThreadListener);
                        destThread =  new ConnectThread(srcinputStream,destoutputStream);
                        destThread.setListener(connectTxThreadListener);
                        fromThread.start();
                        destThread.start();
                    }
                };

                //目标连接建立后开始源连接
                if(result==ConnectionListener.RESULT_SUCCESS){
                    if(connection_type.equals(CONNECT_TCP))
                    {
                        tcp.startServer(port,fromListener);
                    }
                    else if(connection_type.equals(CONNECT_UDP))
                    {
                        udp.startServer(port,fromListener);
                    }

                    connectStateChange(getBaseContext(),STATE_CONNECTED);
                    reconnectcount = 0;
                }
                else if(result==ConnectionListener.RESULT_FAILD)
                {
                    connectStateChange(getBaseContext(),STATE_CONNECT_FAILD);
                }

            }
        };

        //初始化源连接
        if(connection_type.equals(CONNECT_TCP))
        {
            tcp = new TCP();
        }
        else
        {
            udp = new UDP();
        }

        //初始化目标连接
        if(dest_type.equals( DEST_BLUETOOTH))
        {
            bluetooth = new Bluetooth(null,destListener);
        }
        else if(dest_type.equals( DEST_USBOTG))
        {
            fdti = new FDTI(usbManager);
            fdti.setListener(destListener);
        }
    }


    private void readPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        connection_type = preferences.getString("connection_type",CONNECT_TCP);
        dest_type = preferences.getString("destination_type", DEST_BLUETOOTH);
        port = Integer.parseInt(preferences.getString("port", "0"));
        bluetoothDeviceName = preferences.getString("device_name","BTCOM");

        enableGPS = preferences.getBoolean(getString(R.string.key_enable_gps), false);
    }

    private void rx(byte[] data) {
        Intent i = new Intent(ACTION_RX);
        i.putExtra(EXTRA_MESSAGE, data);
        sendBroadcast(i);
    }

    private void tx(byte[] data) {
        Intent i = new Intent(ACTION_TX);
        i.putExtra(EXTRA_MESSAGE, data);
        sendBroadcast(i);
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
        if(tcp!=null)
            tcp.close();
        if(udp!=null)
            udp.close();
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
        if(dest_type.equals( DEST_BLUETOOTH))
        {
            bluetooth.connect(bluetoothDeviceName);
        }
        else
        {
            connectUsb();
        }
        connectStateChange(getBaseContext(),STATE_CONNECTING);
    }

    private void connectUsb()
    {
        HashMap<String, UsbDevice> deviceSet =  usbManager.getDeviceList();
        Log.i(getClass().getSimpleName(), "开始检测USB设备");
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

    private void send(byte[] data) {
        bluetooth.send(data);
        tx(data);
    }


}
