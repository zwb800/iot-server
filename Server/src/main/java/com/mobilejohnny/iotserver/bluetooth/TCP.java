package com.mobilejohnny.iotserver.bluetooth;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by admin2 on 2015/3/20.
 */
public class TCP {

    private TCPListener listener;
    private OutputStream output;
    private Socket socket;
    private ServerSocket serverSocket;

    public  TCP(TCPListener l)  {
        this.listener = l;
    }

    public boolean connect(String ip,int port)
    {
        boolean result = false;
        try {
            socket = new Socket(ip,port);
            output = socket.getOutputStream();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean send(byte[] data)
    {
        boolean result = false;
        try {
            output.write(data);
            output.flush();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean startServer(int port, final TCPListener listener)
    {
        boolean result = false;
        try {
            serverSocket = new ServerSocket(port);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        while(!Thread.currentThread().isInterrupted()){
                            char[] buffer = new char[1024];
                            Socket s = serverSocket.accept();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));

                            while(bufferedReader.read(buffer)!=-1)
                            {
                                listener.onReceive(buffer);
                            }
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
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface TCPListener{
        void onReceive(char[] data);
    }
}
