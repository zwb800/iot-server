package com.mobilejohnny.iotwidget;

/**
 * Created by admin2 on 2015/5/27.
 */
public class WidgetSetting {

    public int color;
    public String buttonLabel;
    public String value;
    public String deviceName;
    public boolean enableBluetooth;
    public boolean enableRemote;


    public WidgetSetting()
    {

    }
    public WidgetSetting(int color,String label,String value,String deviceName,boolean enableRemote,boolean enableBluetooth)
    {
        this.color = color;
        buttonLabel = label;
        this.value = value;
        this.deviceName = deviceName;
        this.enableRemote = enableRemote;
        this.enableBluetooth = enableBluetooth;
    }


}
