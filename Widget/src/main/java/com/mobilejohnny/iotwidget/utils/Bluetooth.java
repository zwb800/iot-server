package com.mobilejohnny.iotwidget.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import com.mobilejohnny.iotwidget.ConnectionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Created by zwb08_000 on 2014/11/4.
 */
public class Bluetooth {
    public static final int RESULT_DEVICE_NOTFOUND = 1;
    public static final int RESULT_FAILD = 2;
    public static final int RESULT_SUCCESS = 3;
    public static final int RESULT_BLUETOOTH_DISABLED = 4;
    public static final int SDK_VER = Build.VERSION.SDK_INT;

    public static final int REQUEST_ENABLE_BLUETOOTH = 5;

    private BluetoothDevice device;
    private static BluetoothAdapter adapter = null;
    private BluetoothSocket socket;


    final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ConnectionListener listener;
    private boolean connected;

    public Bluetooth(Activity context, ConnectionListener listener)
    {
        this.listener = listener;
        adapter = BluetoothAdapter.getDefaultAdapter();
        connected = false;

        if(context!=null&&(!adapter.isEnabled()))
        {
            Intent intentEnableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(intentEnableBluetooth,REQUEST_ENABLE_BLUETOOTH);
        }
    }

    public boolean connect(String deviceName) {

        boolean result = false;
        if(connected)
        {
            close();
        }

        device =  findDeviceByName(deviceName);
        if(adapter!=null&&!adapter.isEnabled()){
            listener.result(RESULT_BLUETOOTH_DISABLED,null,null);
            Log.e("BT", "蓝牙未启用");
        }
        else if(device!=null){
            Log.i("BT","已找到绑定设备");

            if(createSocket()) {
                if(connectSocket()){
                    result = true;
                }
            }

        }
        else
        {
            Log.e("BT", "未找到绑定设备");
        }
        return  result;
    }

    public synchronized boolean send(String deviceName,byte[] data)
    {
        boolean result = false;
        if(connected)
        {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(data);
                Log.i(getClass().getSimpleName(),"已发送");
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(connect(deviceName))
        {
            result = send(deviceName,data);
        }

        return result;
    }

    private boolean createSocket()  {
        boolean result = false;
//        if(tryotherway)
//        {
//            createSocket2();
//            return;
//        }

        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                //sdk 2.3以上需要用此方法连接，否则连接不上，会报 java.io.IOException: Connection refused 异常
                socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            }else {
                socket = (BluetoothSocket)device.getClass()
                        .getMethod("createRfcommSocket",new Class[] { int.class })
                        .invoke(device, 1);
            }
            result = true;
            Log.i("BT", "已创建SOCKET");
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("BT",e.getMessage());
            result = false;
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static boolean  tryotherway =  false;
    private Boolean connectSocket() {



        if(adapter.isDiscovering())
        {
            adapter.cancelDiscovery();
        }

        try {
            Log.i("BT", "开始连接");
            socket.connect();
            Log.i("BT","已连接");
            connected = true;
        } catch (Exception e) {
            Log.e("BT",e.getMessage());
            connected = false;
            closeSocket();
            e.printStackTrace();

//            if(!tryotherway)
//            {
//                if(e.getMessage().equals("Service discovery failed")){
//                    tryotherway = true;
//                    Log.i("BT","尝试另一种方法");
//                    closeSocket();
//                    createSocket();
//                    return connectSocket();
//                }
//            }
//            else
//            {
//                tryotherway = false;
//            }
        }

        return connected;
    }

    private void closeSocket() {
        if(socket!=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close()
    {
        closeSocket();
        socket = null;
        device = null;
        connected = false;
    }

    private BluetoothDevice findDeviceByName(String name) {
        BluetoothDevice device = null;
        Set<BluetoothDevice> devices = getBondedDevices();

        if(devices!=null){
            Iterator<BluetoothDevice> it = devices.iterator();
            while(it.hasNext())
            {
                BluetoothDevice d = it.next();
                if(d.getName().equals(name))
                {
                    device = d;
                    break;
                }
            }
        }


        return device;
    }

    public Set<BluetoothDevice> getBondedDevices() {

        Set<BluetoothDevice> devices = null;
        if(adapter!=null)
            devices = adapter.getBondedDevices();

        return devices;

    }
}

