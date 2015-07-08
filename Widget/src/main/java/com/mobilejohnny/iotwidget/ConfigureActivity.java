package com.mobilejohnny.iotwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Set;


/**
 * The configuration screen for the {@link ButtonAppWidget ButtonAppWidget} AppWidget.
 */
public class ConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;


    private EditText txtButtonLabel;
    private EditText txtButtonValue;
    private Button btnColor;
    private TextView txtDeviceName;
    private CheckBox cbxRemote;
    private CheckBox cbxBluetooth;

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
        txtDeviceName = (TextView)findViewById(R.id.device_name);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);
        cbxRemote = (CheckBox)findViewById(R.id.cbxRemote);
        cbxBluetooth = (CheckBox)findViewById(R.id.cbxBluetooth);

        WidgetSetting setting = WidgetSetting.loadSetting(ConfigureActivity.this, mAppWidgetId);

        btnColor.setOnClickListener(colorClickListener);
        txtButtonLabel.setText(setting.buttonLabel);
        txtDeviceName.setOnClickListener(deviceClickListener);
        color = R.color.blue;
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

    private DialogInterface.OnClickListener colorSelectedListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            btnColor.setBackgroundColor(getResources().getColor(i));
            color = i;
            dialogInterface.dismiss();
        }
    };
    private View.OnClickListener colorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ColorSelectDialogBuilder colorSelectDialogBuilder = new ColorSelectDialogBuilder(ConfigureActivity.this);
            colorSelectDialogBuilder.setOnItemClickListener(colorSelectedListener);
            colorSelectDialogBuilder.build().show();
        }
    };


    private CharSequence[] deviceList;
    private View.OnClickListener deviceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Bluetooth bluetooth = new Bluetooth(null,null);
            Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
            ArrayList<CharSequence> listEntries = new ArrayList<>();
            for (BluetoothDevice device : devices)
            {
                listEntries.add(device.getName());
            }

            deviceList = listEntries.toArray(new CharSequence[listEntries.size()]);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ConfigureActivity.this);
            alertDialogBuilder.setItems(deviceList,deviceSelectedListener).show();
        }
    };

    private DialogInterface.OnClickListener deviceSelectedListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            txtDeviceName.setText(deviceList[i]);
            dialogInterface.dismiss();
        }
    };

    private int color;
    //点击添加按钮
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = ConfigureActivity.this;

            //保存设置
            int buttonColor =  color;
            String buttonLabel = txtButtonLabel.getText().toString();
            String buttonValue = txtButtonValue.getText().toString().replace("\\n", "\n");
            String deviceName = txtDeviceName.getText().toString();
            boolean enableRemote = cbxRemote.isChecked();
            boolean enableBluetooth = cbxBluetooth.isChecked();

            if (!validate()) return;


            WidgetSetting.saveSetting(context, mAppWidgetId,
                    new WidgetSetting(buttonColor, buttonLabel, buttonValue,
                            deviceName, enableRemote, enableBluetooth));

            // 更新Widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ButtonAppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId, false);

            // 设置结果为成功
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    private boolean validate() {
        if(txtButtonLabel.getText().toString().equals(""))
        {
            txtButtonLabel.setError("请输入文本");
            return false;
        }

        if(txtButtonValue.getText().toString().equals("")) {
            txtButtonValue.setError("请输入值");
            return false;
        }

        if((!cbxBluetooth.isChecked()) && (!cbxRemote.isChecked())) {
            cbxBluetooth.setError("请选择连接方式");
            return false;
        }

        if(txtDeviceName.getText().toString().equals("")) {
            txtDeviceName.setError("请选择设备");
            return false;
        }

        return true;
    }

}



