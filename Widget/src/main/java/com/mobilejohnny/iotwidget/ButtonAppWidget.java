package com.mobilejohnny.iotwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ButtonAppWidgetConfigureActivity ButtonAppWidgetConfigureActivity}
 */
public class ButtonAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            ButtonAppWidgetConfigureActivity.deleteSetting(context, appWidgetIds[i]);
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
                                int appWidgetId) {

       WidgetSetting setting = ButtonAppWidgetConfigureActivity.loadSetting(context, appWidgetId);

        int requestCode = appWidgetId;//按widget来区分intent
        Intent intent  = new Intent(ClickReceiver.ACTION_WIDGET_CLICK);
        intent.putExtra(ClickReceiver.EXTRA_VALUE,setting.value);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,requestCode,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.button_app_widget);
        views.setTextViewText(R.id.appwidget_button, setting.buttonLabel);
        views.setOnClickPendingIntent(R.id.appwidget_button,pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


