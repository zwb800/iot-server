package com.mobilejohnny.iotwidget;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by admin2 on 2015/5/27.
 */
public class WidgetSetting {


    public String remoteDeviceID;
    public int color;
    public String buttonLabel;
    public String value;
    public String deviceName;
    public boolean enableBluetooth;
    public boolean enableRemote;

    private static final String PREFS_NAME = "com.mobilejohnny.iotwidget.ButtonAppWidget";
    private static final String PREF_PREFIX_KEY = "hubwidget_";
    private static final String PREF_PREFIX_BUTTON_LABEL = PREF_PREFIX_KEY+ "button_label";
    private static final String PREF_PREFIX_BUTTON_VALUE = PREF_PREFIX_KEY+ "button_value";
    private static final String PREF_PREFIX_BUTTON_COLOR =  PREF_PREFIX_KEY+ "button_color";
    private static final String PREF_PREFIX_BUTTON_DEVICE_NAME = PREF_PREFIX_KEY+ "device_name";
    private static final String PREF_PREFIX_BUTTON_ENABLE_REMOTE = PREF_PREFIX_KEY+ "enable_remote";
    private static final String PREF_PREFIX_BUTTON_ENABLE_BLUETOOTH = PREF_PREFIX_KEY+ "enable_bluetooth";
    private static final String PREF_PREFIX_BUTTON_REMOTE_DEVICEID = PREF_PREFIX_KEY+ "remote_deviceid";


    public WidgetSetting()
    {

    }
    public WidgetSetting(int color,String label,String value,String deviceName,boolean enableRemote,
                         boolean enableBluetooth,
                         String remoteDeviceID)
    {
        this.color = color;
        buttonLabel = label;
        this.value = value;
        this.deviceName = deviceName;
        this.enableRemote = enableRemote;
        this.enableBluetooth = enableBluetooth;
        this.remoteDeviceID = remoteDeviceID;
    }


    static void saveSetting(Context context, int appWidgetId, WidgetSetting setting) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_BUTTON_COLOR+ appWidgetId,setting.color);
        prefs.putString(PREF_PREFIX_BUTTON_LABEL + appWidgetId, setting.buttonLabel);
        prefs.putString(PREF_PREFIX_BUTTON_VALUE + appWidgetId, setting.value);
        prefs.putString(PREF_PREFIX_BUTTON_DEVICE_NAME + appWidgetId, setting.deviceName);
        prefs.putBoolean(PREF_PREFIX_BUTTON_ENABLE_REMOTE + appWidgetId, setting.enableRemote);
        prefs.putBoolean(PREF_PREFIX_BUTTON_ENABLE_BLUETOOTH + appWidgetId, setting.enableBluetooth);
        prefs.putString(PREF_PREFIX_BUTTON_REMOTE_DEVICEID+appWidgetId,setting.remoteDeviceID);
        prefs.commit();
    }

    static WidgetSetting loadSetting(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        WidgetSetting setting = new WidgetSetting();
        setting.color = prefs.getInt(PREF_PREFIX_BUTTON_COLOR + appWidgetId, android.R.color.transparent);
        setting.buttonLabel = prefs.getString(PREF_PREFIX_BUTTON_LABEL + appWidgetId, "");
        setting.value = prefs.getString(PREF_PREFIX_BUTTON_VALUE + appWidgetId, "");
        setting.deviceName = prefs.getString(PREF_PREFIX_BUTTON_DEVICE_NAME + appWidgetId, "");
        setting.enableBluetooth = prefs.getBoolean(PREF_PREFIX_BUTTON_ENABLE_BLUETOOTH + appWidgetId, false);
        setting.enableRemote = prefs.getBoolean(PREF_PREFIX_BUTTON_ENABLE_REMOTE+appWidgetId,false);
        setting.remoteDeviceID = prefs.getString(PREF_PREFIX_BUTTON_REMOTE_DEVICEID+appWidgetId,"");
        return setting;
    }

    static void deleteSetting(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_BUTTON_LABEL + appWidgetId);
        prefs.remove(PREF_PREFIX_BUTTON_VALUE + appWidgetId);
        prefs.remove(PREF_PREFIX_BUTTON_COLOR + appWidgetId);
        prefs.remove(PREF_PREFIX_BUTTON_DEVICE_NAME + appWidgetId);
        prefs.remove(PREF_PREFIX_BUTTON_ENABLE_REMOTE + appWidgetId);
        prefs.remove(PREF_PREFIX_BUTTON_ENABLE_BLUETOOTH + appWidgetId);

        prefs.commit();
    }


}
