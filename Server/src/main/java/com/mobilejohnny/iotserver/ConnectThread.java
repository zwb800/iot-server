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
        while (!Thread.currentThread().isInterrupted())
        {
            int len = -1;
            try {
                byte[] buffer = new byte[1024*8];
                while((len = inputStream.read(buffer))!=-1)
                {
                    outputStream.write(buffer,0,len);

                    if(listener!=null)
                    {
//                        String str = new String(buffer,0,len);
                        byte[] out = new byte[len];
                        for (int i=0;i<len;i++)
                        {
                            out[i] = buffer[i];
                        }
                        listener.onReceive(out);
                    }
                }
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
    }

    public interface ConnectThreadListener{
        void onReceive(byte[] data);
    }
}
