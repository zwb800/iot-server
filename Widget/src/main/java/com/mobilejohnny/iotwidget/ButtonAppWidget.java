package com.mobilejohnny.iotwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.mobilejohnny.iotwidget.utils.Request;


import java.io.IOException;
import java.util.ArrayList;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ConfigureActivity ButtonAppWidgetConfigureActivity}
 */
public class ButtonAppWidget extends AppWidgetProvider {

    public static final String EXTRA_APPWIDGETID = "appwidgetid";

    private static final String APP_SECRET = "TOrA9OlkZYhMysgsHtmT5w==";
    private static final String APP_PACKAGE = "com.mobilejohnny.iotserver" ;



    public  static final String ACTION_WIDGET_CLICK = "com.mobilejohnny.iotwidget.ButtonAppWidget.ACTION_WIDGET_CLICK";
    private static final String ACTION_WIDGET_CLICKED = "com.mobilejohnny.iotwidget.ButtonAppWidget.ACTION_WIDGET_CLICKED";

    public static final String EXTRA_VALUE = "VALUE";
    private static final String EXTRA_ENABLE_BLUETOOTH = "enable_bluetooth";
    private static final String EXTRA_ENABLE_REMOTE = "enable_remote";
    public static final String EXTRA_DEVICENAME = "com.mobilejohnny.iotwidget.extra.EXTRA_DEVICENAME";
    private static final String EXTRA_REMOTE_DEVICEID = "remote_deviceid";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        if(action.equals(ACTION_WIDGET_CLICK))
        {
            int appWdigetID = intent.getIntExtra(ButtonAppWidget.EXTRA_APPWIDGETID, -1);
            String value = intent.getStringExtra(EXTRA_VALUE);
            Boolean remote = intent.getBooleanExtra(EXTRA_ENABLE_REMOTE, true);
            Boolean bluetooth = intent.getBooleanExtra(EXTRA_ENABLE_BLUETOOTH, false);
            String deviceName = intent.getStringExtra(EXTRA_DEVICENAME);
            String remoteDeviceID = intent.getStringExtra(EXTRA_REMOTE_DEVICEID);

            updateButton(context,appWdigetID,true);

            if(remote)
                sendMessageViaXMPush(context, appWdigetID,remoteDeviceID, value);
            if(bluetooth)
                sendMessageViaBluetooth(context, appWdigetID, deviceName, value);
        }
        else if(action.equals(ACTION_WIDGET_CLICKED))
        {
            int appWdigetID = intent.getIntExtra(ButtonAppWidget.EXTRA_APPWIDGETID, 0);
            updateButton(context, appWdigetID, false);
        }
    }

    private void updateButton(Context context,int appWdigetID,boolean processing) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ButtonAppWidget.updateAppWidget(context, appWidgetManager, appWdigetID, processing);
    }

    public static void startActionClicked(Context context,int appWidgetID)
    {
        Intent intent = new Intent(ACTION_WIDGET_CLICKED);
        intent.putExtra(ButtonAppWidget.EXTRA_APPWIDGETID,appWidgetID);
        context.sendBroadcast(intent);
    }

    private void sendMessageViaBluetooth(Context context,int appWidgetID,String deviceName, final String value) {
        BluetoothService.startActionSend(context, appWidgetID, deviceName, value.getBytes());
    }

    private void sendMessageViaXMPush(final Context context, final int appWidgetID,
                                      final String remoteDeviceID, final String msg)
    {
//        final ArrayList<NameValuePair> parameters = new ArrayList<>();
//        parameters.add(new BasicNameValuePair("id", remoteDeviceID));
//        parameters.add(new BasicNameValuePair("msg",msg));
        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... voids) {
                String url = Constants.SEND_URL+
                        "&alias="+remoteDeviceID+"&msg="+msg;
                String content = Request.post(url);
                Log.i("",content);
                return content.equals("true");
            }

            @Override
            protected void onPostExecute(Boolean b) {
                updateButton(context, appWidgetID, false);
                Toast.makeText(context, "发送" + (b ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i],false);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            WidgetSetting.deleteSetting(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId,boolean processing) {

       WidgetSetting setting = WidgetSetting.loadSetting(context, appWidgetId);

        int requestCode = appWidgetId;//按widget来区分intent
        Intent intent  = new Intent(ACTION_WIDGET_CLICK);
        intent.putExtra(EXTRA_APPWIDGETID,appWidgetId);
        intent.putExtra(EXTRA_VALUE,setting.value);
        intent.putExtra(EXTRA_ENABLE_BLUETOOTH,setting.enableBluetooth);
        intent.putExtra(EXTRA_ENABLE_REMOTE,setting.enableRemote);
        intent.putExtra(EXTRA_DEVICENAME,setting.deviceName);
        intent.putExtra(EXTRA_REMOTE_DEVICEID,setting.remoteDeviceID);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,requestCode,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.button_app_widget);
        Log.i("updateWidget", setting.buttonLabel);
        if(processing)
        {
            views.setViewVisibility(R.id.appwidget_button, View.GONE);
            views.setViewVisibility(R.id.appwidget_progressBar,View.VISIBLE);
        }
        else
        {
            views.setViewVisibility(R.id.appwidget_button, View.VISIBLE);
            views.setViewVisibility(R.id.appwidget_progressBar,View.GONE);
        }

        views.setInt(R.id.appwidget_button, "setBackgroundResource", setting.color);
        views.setOnClickPendingIntent(R.id.appwidget_button,pendingIntent);
        views.setTextViewText(R.id.appwidget_button, setting.buttonLabel);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


