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
    private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket;



    final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothListener listener;


    public Bluetooth(String deviceName)
    {
        adapter.startDiscovery();
        device =  findDeviceByName(deviceName);
    }

    AsyncTask<Object,Void,Integer> task = new AsyncTask<Object,Void,Integer>() {
        @Override
        protected  Integer doInBackground(Object[] handler) {

            int result = RESULT_FAILD;
            String data = null;
            if(handler!=null&&handler.length>0){
                data = (String) handler[0];
            }

            if(connectSocket())
            {
                try {
                    OutputStream out = socket.getOutputStream();
                    for (int i = 0; i < data.length(); i++) {
                        out.write(data.charAt(i));
                    }
                    out.flush();
                    out.close();
                    result = RESULT_SUCCESS;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            closeSocket();
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {

            super.onPostExecute(result);
            if(listener!=null){
                listener.result(result);
            }

        }
    };

    public void setListener(BluetoothListener listener)
    {
        this.listener = listener;
    }

    public void connect(String data) {

        if(!adapter.isEnabled()){
            listener.result(RESULT_BLUETOOTH_DISABLED);
            Log.i("BT","蓝牙未启用");
        }
        else if(device!=null){
            Log.i("BT","已找到绑定设备");
            createSocket();

            if(socket!=null)
            {
                task.execute(data);
            }
            else
            {
                listener.result(RESULT_FAILD);
                Log.i("BT","SOCKET创建失败");
            }
        }
        else
        {
            listener.result(RESULT_DEVICE_NOTFOUND);
            Log.i("BT", "未找到绑定设备");
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void createSocket() {

        if(tryotherway)
        {
            createSocket2();
            return;
        }

        try {

            int sdk = Build.VERSION.SDK_INT;
            if(sdk >= Build.VERSION_CODES.HONEYCOMB){
                //sdk 2.3以上需要用此方法连接，否则连接不上，会报 java.io.IOException: Connection refused 异常
                socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            }else {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            }
            Log.i("BT","已创建SOCKET");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createSocket2() {
        try {
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            socket = (BluetoothSocket) m.invoke(device, 1);
            Log.i("BT","已创建SOCKET2");
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

        try {
            Log.i("BT","开始连接");
            socket.connect();
            success = true;
            Log.i("BT","已连接");
        } catch (IOException e) {
            e.printStackTrace();
            closeSocket();
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
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BluetoothDevice findDeviceByName(String name) {
        BluetoothDevice device = null;
        Set<BluetoothDevice> devices = getBondedDevices();
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
        return device;
    }

    public static Set<BluetoothDevice> getBondedDevices() {
        return adapter.getBondedDevices();

    }
}
