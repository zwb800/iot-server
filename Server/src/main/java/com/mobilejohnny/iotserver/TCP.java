package com.mobilejohnny.iotserver;

import com.mobilejohnny.iotserver.ConnectThread;

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
    private BufferedWriter bufferedWriter;

    public  TCP(TCPListener l)  {
        this.listener = l;
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

                            socket = serverSocket.accept();
                            listener.onConnected(socket);
//                            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                            char[] buffer = new char[1024*8];
//                            int len = -1;
//                            while((len = bufferedReader.read(buffer))!=-1)
//                            {
//                                listener.onReceive(new String(buffer,0,len));
//                            }
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

    public Socket getSocket() {
        return socket;
    }

    public interface TCPListener{
        void onConnected(Socket socket);
    }
}
