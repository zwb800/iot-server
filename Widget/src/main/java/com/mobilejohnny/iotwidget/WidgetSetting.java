package com.mobilejohnny.iotwidget;

/**
 * Created by admin2 on 2015/5/27.
 */
public class WidgetSetting {
    public WidgetSetting()
    {

    }
    public WidgetSetting(String label,String value)
    {
        buttonLabel = label;
        this.value = value;
    }

    public String buttonLabel;
    public String value;
}
