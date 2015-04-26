package com.mobilejohnny.iotserver;

import android.util.Log;

import java.io.*;
import java.net.Socket;

/**
 * Created by admin2 on 2015/3/21.
 */
public class ConnectThread extends Thread {

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private ConnectThreadListener listener;
    public byte[] asyncData;


    public ConnectThread(InputStream inputStream,OutputStream outputStream)
    {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void setListener(ConnectThreadListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void run() {
        super.run();
        try {
            while (!Thread.currentThread().isInterrupted())
            {
                int len = -1;

                byte[] buffer = new byte[1024];
                while((len = inputStream.read(buffer))!=-1)
                {

                    if(len==22)
                    {
                        byte[] aData = getAsyncData();
                        if(aData!=null&&aData.length>0)
                        {

                            for (int i=0;i<aData.length;i++)
                            {
                                buffer[len+i] = aData[i];
                            }

                            len+= aData.length;
                            setAnsyData(null);
                        }

                    }
                    Log.i("ConnectThread",len+"");
                    outputStream.write(buffer,0,len);
                    if(listener!=null)
                    {
                        byte[] out = new byte[len];
                        for (int i=0;i<len;i++)
                        {
                            out[i] = buffer[i];
                        }
                        listener.onReceive(out);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAnsyData(byte[] msp_gps) {
        synchronized (this)
        {
            asyncData = msp_gps;
        }
    }

    private byte[] getAsyncData()
    {
        synchronized (this)
        {
            return asyncData;
        }
    }

    public interface ConnectThreadListener{
        void onReceive(byte[] data);
    }
}
