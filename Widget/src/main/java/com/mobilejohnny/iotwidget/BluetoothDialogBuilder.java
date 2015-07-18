package com.mobilejohnny.iotwidget;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.GridView;
import com.mobilejohnny.iotwidget.utils.Bluetooth;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by admin2 on 2015/7/17.
 */
public class BluetoothDialogBuilder {

    private final Context context;
    private DialogInterface.OnClickListener deviceSelectedListener;

    public BluetoothDialogBuilder(Context context)
    {
        this.context = context;
    }

    private CharSequence[] deviceList;

    public CharSequence[] getDeviceList()
    {
        return deviceList;
    }

    public AlertDialog build() {

        Bluetooth bluetooth = new Bluetooth(null,null);
        Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
        ArrayList<CharSequence> listEntries = new ArrayList<>();
        for (BluetoothDevice device : devices)
        {
            listEntries.add(device.getName());
        }

        deviceList = listEntries.toArray(new CharSequence[listEntries.size()]);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setItems(deviceList, deviceSelectedListener);
        return alertDialogBuilder.create();
    }

    public void setDeviceSelectedListener(DialogInterface.OnClickListener deviceSelectedListener) {
        this.deviceSelectedListener = deviceSelectedListener;
    }
}
