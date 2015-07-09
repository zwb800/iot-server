package com.mobilejohnny.iotserver.utils;

import com.mobilejohnny.iotserver.ConnectionListener;
import com.mobilejohnny.iotserver.utils.Bluetooth;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by admin2 on 2015/3/20.
 */
public class TCP {

    private OutputStream output;
    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedWriter bufferedWriter;

    public  TCP()  {
    }

    public boolean send(String data)
    {
        boolean result = false;
        try {
            bufferedWriter.write(data);
            bufferedWriter.flush();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public OutputStream getOutputStream()
    {
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    public boolean startServer(int port, final ConnectionListener listener)
    {
        boolean result = false;
        try {
            serverSocket = new ServerSocket(port);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        while(!Thread.currentThread().isInterrupted()){

                            socket = serverSocket.accept();
                            listener.result(Bluetooth.RESULT_SUCCESS, socket.getInputStream(), socket.getOutputStream());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();



            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void close()
    {
        if(serverSocket!=null){
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
