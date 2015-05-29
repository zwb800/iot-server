package com.mobilejohnny.iotwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


/**
 * The configuration screen for the {@link ButtonAppWidget ButtonAppWidget} AppWidget.
 */
public class ButtonAppWidgetConfigureActivity extends Activity {


    private static final int REQUESTCODE_PICKCOLOR = 1;
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static final String PREFS_NAME = "com.mobilejohnny.iotwidget.ButtonAppWidget";
    private static final String PREF_PREFIX_KEY = "hubwidget_";
    private static final String PREF_PREFIX_BUTTON_LABEL = PREF_PREFIX_KEY+ "button_label";
    private static final String PREF_PREFIX_BUTTON_VALUE = PREF_PREFIX_KEY+ "button_value";
    private static final String PREF_PREFIX_BUTTON_COLOR =  PREF_PREFIX_KEY+ "button_color";

    private EditText txtButtonLabel;
    private EditText txtButtonValue;
    private Button btnColor;
    private int color;


    public ButtonAppWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //按返回会取消建立Widget
        setResult(RESULT_CANCELED);

        // 获得AppWidgetID
        mAppWidgetId = getAppWidgetID();

        //AppWidgetID无效退出
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        setContentView(R.layout.button_app_widget_configure);
        btnColor = (Button)findViewById(R.id.button_color);
        txtButtonLabel = (EditText) findViewById(R.id.button_label);
        txtButtonValue = (EditText) findViewById(R.id.button_value);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        WidgetSetting setting = loadSetting(ButtonAppWidgetConfigureActivity.this, mAppWidgetId);

        btnColor.setOnClickListener(colorClickListener);
        txtButtonLabel.setText(setting.buttonLabel);
    }

    private int getAppWidgetID() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
             return extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        return AppWidgetManager.INVALID_APPWIDGET_ID;
    }

    private View.OnClickListener colorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getBaseContext(),ColorSelectDialog.class);
            startActivityForResult(intent,REQUESTCODE_PICKCOLOR);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            if(requestCode == REQUESTCODE_PICKCOLOR)
            {
                color = data.getIntExtra(ColorSelectDialog.EXTRA_COLOR,R.color.blue);

                btnColor.setBackgroundColor(getResources().getColor(color));
            }
        }

    }

    //点击添加按钮
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = ButtonAppWidgetConfigureActivity.this;

            //保存设置
            int buttonColor =  color;
            String buttonLabel = txtButtonLabel.getText().toString();
            String buttonValue = txtButtonValue.getText().toString().replace("\\n","\n");
            saveSetting(context, mAppWidgetId, new WidgetSetting(buttonColor,buttonLabel, buttonValue));

            // 更新Widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ButtonAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId,false);

            // 设置结果为成功
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    static void saveSetting(Context context, int appWidgetId, WidgetSetting setting) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_BUTTON_COLOR+ appWidgetId,setting.color);
        prefs.putString(PREF_PREFIX_BUTTON_LABEL + appWidgetId, setting.buttonLabel);
        prefs.putString(PREF_PREFIX_BUTTON_VALUE + appWidgetId, setting.value);
        prefs.commit();
    }

    static WidgetSetting loadSetting(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        WidgetSetting setting = new WidgetSetting();
        setting.color = prefs.getInt(PREF_PREFIX_BUTTON_COLOR+appWidgetId,android.R.color.transparent);
        setting.buttonLabel = prefs.getString(PREF_PREFIX_BUTTON_LABEL + appWidgetId, "");
        setting.value = prefs.getString(PREF_PREFIX_BUTTON_VALUE + appWidgetId, "");
        return setting;
    }

    static void deleteSetting(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_BUTTON_LABEL + appWidgetId);
        prefs.remove(PREF_PREFIX_BUTTON_VALUE + appWidgetId);
        prefs.commit();
    }
}



