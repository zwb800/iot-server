package com.mobilejohnny.iotwidget;

/**
 * Created by admin2 on 2015/5/27.
 */
public class WidgetSetting {
    public int color;

    public WidgetSetting()
    {

    }
    public WidgetSetting(int color,String label,String value)
    {
        this.color = color;
        buttonLabel = label;
        this.value = value;
    }

    public String buttonLabel;
    public String value;
}
