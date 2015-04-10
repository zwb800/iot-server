package com.mobilejohnny.iotserver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;


public class UsbHostActivity extends ActionBarActivity {

    public  static  final String USB_PERMISSION = "com.mobilejohnny.iotserver.USB_PERMISSION";
    private UsbBroadcastReceiver usbReceiver;
    private UsbManager manager;
    private UsbDevice device;
    private int bcdDevice = 1;//FT232RL
    private int numOfChannels = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_host);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,new Intent(USB_PERMISSION),0);
        usbReceiver = new UsbBroadcastReceiver();
        registerReceiver( usbReceiver,new IntentFilter(USB_PERMISSION));

        manager = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> deviceSet =  manager.getDeviceList();
        Log.i(getClass().getSimpleName(),"开始检测USB设备");
        for (UsbDevice device:deviceSet.values())
        {
            Log.i(this.getClass().getSimpleName(),device.getDeviceName()+" "+device.getVendorId()+" "+device.getProductId());
            Log.i(this.getClass().getSimpleName(),"hasPermission:"+manager.hasPermission(device));
            if(!manager.hasPermission(device))
            {
                manager.requestPermission(device,pendingIntent);
            }
            else
            {
                this.device = device;
                begin();
            }
        }
    }

    private void begin() {

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(usbReceiver);
        super.onDestroy();
    }

    private class UsbBroadcastReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(USB_PERMISSION))
            {
                synchronized (this)
                {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false))
                    {
                        if(device!=null)
                        {
                            UsbHostActivity.this.device = device;
                            begin();
                        }
                    }
                    else
                    {
                        Log.i(this.getClass().getSimpleName(),"Permission Denied");
                    }
                }
            }
        }
    }
}
