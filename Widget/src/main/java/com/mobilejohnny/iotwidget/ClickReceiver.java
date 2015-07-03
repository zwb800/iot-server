package com.mobilejohnny.iotwidget;

import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Sender;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClickReceiver extends BroadcastReceiver {

    private static final String APP_SECRET = "TOrA9OlkZYhMysgsHtmT5w==";
    private static final String APP_PACKAGE = "com.mobilejohnny.iotserver" ;



    public  static final String ACTION_WIDGET_CLICK = "com.mobilejohnny.iotwidget.ButtonAppWidget.ACTION_WIDGET_CLICK";
    private static final String ACTION_WIDGET_CLICKED = "com.mobilejohnny.iotwidget.ButtonAppWidget.ACTION_WIDGET_CLICKED";

    public static final String EXTRA_VALUE = "VALUE";
    private static final String EXTRA_ENABLE_BLUETOOTH = "enable_bluetooth";
    private static final String EXTRA_ENABLE_REMOTE = "enable_remote";




    public ClickReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
       String action = intent.getAction();

        if(action.equals(ACTION_WIDGET_CLICK))
        {
            String value = intent.getStringExtra(EXTRA_VALUE);
            Boolean remote = intent.getBooleanExtra(EXTRA_ENABLE_REMOTE,false);
            Boolean bluetooth = intent.getBooleanExtra(EXTRA_ENABLE_BLUETOOTH,false);

            int appWdigetID = intent.getIntExtra(ButtonAppWidget.EXTRA_APPWIDGETID, 0);
            updateButton(context,appWdigetID,true);

            if(remote)
                sendMessage(context,appWdigetID,value);
            if(bluetooth) {

                sendMessageViaBluetooth(context, appWdigetID, "", value);
            }
        }
        else  if(action.equals(ACTION_WIDGET_CLICKED))
        {
            int appWdigetID = intent.getIntExtra(ButtonAppWidget.EXTRA_APPWIDGETID, 0);
            updateButton(context, appWdigetID, false);
        }
        else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
        {
            BluetoothService.startActionReconnect(context);
            BluetoothService.connectStateChange(context,BluetoothService.STATE_DISCONNECTED);

            Log.i(getClass().getSimpleName(),"连接重连中...");
        }

    }

    public static void startActionClicked(Context context,int appWidgetID)
    {
        Intent intent = new Intent(ACTION_WIDGET_CLICKED);
        intent.putExtra(ButtonAppWidget.EXTRA_APPWIDGETID,appWidgetID);
        context.sendBroadcast(intent);
    }

    private void sendMessageViaBluetooth(Context context,int appWidgetID,String deviceName, final String value) {
        BluetoothService.startActionSend(context, appWidgetID, value.getBytes());

        final Bluetooth bluetooth = new Bluetooth(null, new ConnectionListener() {
            @Override
            public void result(int result, InputStream in, OutputStream out) {

            }
        });

        bluetooth.connect(deviceName);
    }

    private void updateButton(Context context,int appWdigetID,boolean processing) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ButtonAppWidget.updateAppWidget(context, appWidgetManager, appWdigetID, processing);
    }

    private void sendMessage(final Context context,final int appWidgetID,String msg)
    {
        Constants.useOfficial();
        final Sender sender = new Sender(APP_SECRET);
        final Message message = new Message.Builder()
                .payload(msg)
                .passThrough(1)
                .timeToLive(0)
                .restrictedPackageName(APP_PACKAGE)
                .build();

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean result = false;
                try {
                    sender.broadcastAllNoRetry(message);
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(getClass().getSimpleName(),e.getMessage());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                updateButton(context,appWidgetID,false);
                Toast.makeText(context, "发送"+(b?"成功":"失败"),Toast.LENGTH_SHORT).show();

            }
        }.execute();
    }

}
