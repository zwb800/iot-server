package com.mobilejohnny.iotwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ClickReceiver extends BroadcastReceiver {

    public  static final String ACTION_WIDGET_CLICK = "com.mobilejohnny.iotwidget.ButtonAppWidget.ACTION_WIDGET_CLICK";
    public static final String EXTRA_VALUE = "VALUE";

    public ClickReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
       String action = intent.getAction();
        if(action.equals(ACTION_WIDGET_CLICK))
        {
            String value = intent.getStringExtra(EXTRA_VALUE);
            Toast.makeText(context,value,Toast.LENGTH_LONG).show();

        }
    }
}
