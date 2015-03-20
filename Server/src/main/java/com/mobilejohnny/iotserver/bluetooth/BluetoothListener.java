package com.mobilejohnny.iotserver.bluetooth;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by zwb08_000 on 2014/11/4.
 */
public interface BluetoothListener {
    public void result(int result);

    void onReceive(String s);
}
