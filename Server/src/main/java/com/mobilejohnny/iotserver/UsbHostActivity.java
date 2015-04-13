package com.mobilejohnny.iotserver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;


public class UsbHostActivity extends ActionBarActivity {

    public  static  final String USB_PERMISSION = "com.mobilejohnny.iotserver.USB_PERMISSION";
    private UsbBroadcastReceiver usbReceiver;
    private UsbManager manager;
    private FDTI fdti;
    private OutputStream outputStream;
    private InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_host);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,new Intent(USB_PERMISSION),0);
        usbReceiver = new UsbBroadcastReceiver();
        registerReceiver( usbReceiver,new IntentFilter(USB_PERMISSION));

        manager = (UsbManager) getSystemService(USB_SERVICE);
        fdti = new FDTI(manager);
        fdti.setListener(new Bluetooth.BluetoothListener() {
            @Override
            public void result(int result) {

            }

            @Override
            public void onConnected(InputStream inputStream, OutputStream outputStream) {
                UsbHostActivity.this.inputStream = inputStream;
                UsbHostActivity.this.outputStream  = outputStream;
                threadRead.start();
                threadWrite.start();
            }
        });

        HashMap<String, UsbDevice> deviceSet =  manager.getDeviceList();
        Log.i(getClass().getSimpleName(),"开始检测USB设备");
        for (UsbDevice device:deviceSet.values())
        {

            Log.i(this.getClass().getSimpleName(),"hasPermission:"+manager.hasPermission(device));
            if(!manager.hasPermission(device))
            {
                manager.requestPermission(device,pendingIntent);
            }
            else
            {

                begin(device);
            }
        }
    }

    private void begin(UsbDevice device) {
        fdti.begin(device);

    }

    private boolean threadStop = false;
    Thread threadWrite =  new Thread(new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[1];
            try {
                buffer[0] = 40;
                while (!threadStop) {
                    outputStream.write(buffer);
                    Thread.sleep(1000);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    });

    Thread threadRead = new Thread(new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            while(!threadStop)
            {
                try {
                    while (!threadStop) {

                        if((len = inputStream.read(buffer))!=-1)
                        {
                            Log.i(this.getClass().getSimpleName(),"Receive:"+new String(buffer,0,len));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    });

    @Override
    protected void onDestroy() {
        unregisterReceiver(usbReceiver);
        fdti.close();
        threadStop = true;
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
                            begin(device);
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
