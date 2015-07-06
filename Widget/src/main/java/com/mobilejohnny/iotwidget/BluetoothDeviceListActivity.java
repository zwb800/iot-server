package com.mobilejohnny.iotwidget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by admin2 on 2015/3/25.
 */
public class BluetoothDeviceListActivity extends Activity {
    public static final String EXTRA_DEVICENAME = "EXTRA_DEVICENAME";
    private Bluetooth bluetooth;
    private ListView deviceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.bluetooth_devicelist);
        deviceList = (ListView) findViewById(R.id.device_list);

        bluetooth = new Bluetooth(null,null);
        Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
        ArrayList<String> listEntries = new ArrayList<String>();
        for (BluetoothDevice device : devices)
        {
            listEntries.add(device.getName());
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listEntries.toArray(new String[listEntries.size()]));

        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(onItemClick);
    }

    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = getIntent();
            intent.putExtra(EXTRA_DEVICENAME,(String)adapterView.getItemAtPosition(i));
            setResult(RESULT_OK, intent);
            finish();
        }
    };
}
