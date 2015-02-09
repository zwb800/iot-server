package com.mobilejohnny.iotserver;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.mobilejohnny.iotserver.bluetooth.Bluetooth;
import com.mobilejohnny.iotserver.bluetooth.BluetoothListener;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BluetoothService extends Service {

    private static final String ACTION_CONNECT = "com.mobilejohnny.iotserver.action.CONNECT";

    // TODO: send message
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SEND = "com.mobilejohnny.iotserver.action.SEND";

    // TODO: message
    private static final String EXTRA_PARAM_MESSAGE = "com.mobilejohnny.iotserver.extra.PARAM_MESSAGE";

    Bluetooth bluetooth = new Bluetooth();

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSend(Context context, String message) {
        message = "\n"+message+"+D";

        Intent intent = new Intent(context, BluetoothService.class);
        intent.setAction(ACTION_SEND);
        intent.putExtra(EXTRA_PARAM_MESSAGE, message);
        context.startService(intent);
    }

    public static void startActionConnect(Context context) {

        Intent intent = new Intent(context, BluetoothService.class);
        intent.setAction(ACTION_CONNECT);
        context.startService(intent);
    }



    public BluetoothService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        bluetooth.close();
        bluetooth = null;
        super.onLowMemory();

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
                final String message = intent.getStringExtra(EXTRA_PARAM_MESSAGE);
                handleActionMessage(message);
            }
            else if (ACTION_CONNECT.equals(action)) {
                handleActionConnect();
            }

        }
        else
        {
            handleActionConnect();
        }
        return Service.START_STICKY;
    }

    private void handleActionConnect() {
        connect();
    }

    private void handleActionMessage(String message) {
        send(message);
    }

    private void connect() {

        bluetooth.connect("315",new BluetoothListener() {
            @Override
            public void result(int result) {

            }
        });
    }

    private void send(String message) {

        bluetooth.send(message, new BluetoothListener() {
            @Override
            public void result(int result) {
                Toast.makeText(BluetoothService.this,result==Bluetooth.RESULT_SUCCESS?"成功":"失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
