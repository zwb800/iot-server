package com.mobilejohnny.iotwidget;

import android.appwidget.AppWidgetManager;
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

public class ClickReceiver extends BroadcastReceiver {

    public  static final String ACTION_WIDGET_CLICK = "com.mobilejohnny.iotwidget.ButtonAppWidget.ACTION_WIDGET_CLICK";
    public static final String EXTRA_VALUE = "VALUE";

    private static final String APP_SECRET = "TOrA9OlkZYhMysgsHtmT5w==";
    private static final String APP_PACKAGE = "com.mobilejohnny.iotserver" ;
    private int appWdigetID;

    public ClickReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
       String action = intent.getAction();
        if(action.equals(ACTION_WIDGET_CLICK))
        {
            String value = intent.getStringExtra(EXTRA_VALUE);
            appWdigetID = intent.getIntExtra(ButtonAppWidget.EXTRA_APPWIDGETID, 0);
            sendMessage(context,value);

            // 更新Widget
            updateButton(context,true);
        }
    }

    private void updateButton(Context context,boolean processing) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ButtonAppWidget.updateAppWidget(context, appWidgetManager, appWdigetID, processing);
    }

    private void sendMessage(final Context context,String msg)
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
                    Log.e("",e.getMessage());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                updateButton(context,false);
                Toast.makeText(context, "发送"+(b?"成功":"失败"),Toast.LENGTH_SHORT).show();

            }
        }.execute();
    }

}
