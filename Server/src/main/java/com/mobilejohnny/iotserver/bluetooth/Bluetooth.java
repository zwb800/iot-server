package com.mobilejohnny.iotserver.bluetooth;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private BluetoothDevice device;
    private static BluetoothAdapter adapter = null;
    private BluetoothSocket socket;


    final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothListener listener;
    private boolean connected;


    public Bluetooth()
    {
        adapter = BluetoothAdapter.getDefaultAdapter();
        connected = false;
    }

    public void connect(String deviceName, final BluetoothListener listener) {
        device =  findDeviceByName(deviceName);
        if(adapter!=null&&!adapter.isEnabled()){
            listener.result(RESULT_BLUETOOTH_DISABLED);
            Log.e("BT", "蓝牙未启用");
        }
        else if(device!=null){
            Log.i("BT","已找到绑定设备");
            new AsyncTask<Void,Void,Void> (){

                @Override
                protected Void doInBackground(Void... voids) {
                    int result = RESULT_FAILD;
                    if(createSocket()) {
                        if(connectSocket()){
                            result = RESULT_SUCCESS;
                        }
                    }
                    listener.result(result);
                    return null;
                }
            }.execute();
        }
        else
        {
            listener.result(RESULT_DEVICE_NOTFOUND);
            Log.e("BT", "未找到绑定设备");
        }
    }

    public void send(final String data, final BluetoothListener listener)
    {
        if(socket!=null)
        {
            AsyncTask<Void,Void,Integer> task = new AsyncTask<Void,Void,Integer>() {
                @Override
                protected  Integer doInBackground(Void... voids) {

                    int result = RESULT_FAILD;
                    if(createSocket()) {
                        if (connectSocket()) {
                            try {
                                OutputStream out = socket.getOutputStream();
                                for (int i = 0; i < data.length(); i++) {
                                    out.write(data.charAt(i));
                                }
                                out.flush();
                                result = RESULT_SUCCESS;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    return result;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);
                    if(listener!=null){
                        listener.result(result);
                    }

                }
            }.execute();
        }
        else
        {
            listener.result(RESULT_FAILD);
            Log.e("BT","SOCKET创建失败");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean createSocket() {
        boolean result = false;
//        if(tryotherway)
//        {
//            createSocket2();
//            return;
//        }

        if(connected)
            return true;

        try {
            int sdk = Build.VERSION.SDK_INT;
            if(sdk >= Build.VERSION_CODES.HONEYCOMB){
                //sdk 2.3以上需要用此方法连接，否则连接不上，会报 java.io.IOException: Connection refused 异常
                socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            }else {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);

            }
            result = true;
            Log.i("BT", "已创建SOCKET");
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    private void createSocket2() {
        try {
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            socket = (BluetoothSocket) m.invoke(device, 1);
            Log.i("BT", "已创建SOCKET2");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }



    private static boolean  tryotherway =  false;
    private Boolean connectSocket() {
        boolean success = false;
        if(adapter.isDiscovering())
        {
            adapter.cancelDiscovery();
        }

        if(connected)
            return true;

        try {
            Log.i("BT","开始连接");
            socket.connect();
            success = true;
            Log.i("BT","已连接");
            connected = true;
        } catch (IOException e) {
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

        return success;
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

    public static Set<BluetoothDevice> getBondedDevices() {

        Set<BluetoothDevice> devices = null;
        if(adapter!=null)
            devices = adapter.getBondedDevices();

        return devices;

    }
}
