package com.mobilejohnny.iotserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectionReceiver extends BroadcastReceiver {



    public ConnectionReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
        {

            ConnectionService.startActionReconnect(context);//开始重连
            ConnectionService.connectStateChange(context,ConnectionService.STATE_DISCONNECTED);

            Log.i(getClass().getSimpleName(),"连接重连中...");
        }

    }
}
