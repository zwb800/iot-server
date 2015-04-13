package com.mobilejohnny.iotserver;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

/**
 * Created by zwb08_000 on 2015/3/21.
 */
public class UDP {

    private OutputStream outputStream;
    private InputStream inputStream;
    private DatagramSocket datagramSocket;
    private InetAddress address;
    private int port;
    byte[] buffer = new byte[1024];
    DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length);
    public UDP()
    {

    }

    public InputStream getInputStream()
    {
        return  inputStream;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public boolean startServer(int port,UDPListener listener)
    {
        try {
            this.port = port;
            datagramSocket = new DatagramSocket(port);

            inputStream = new InputStream() {
                int len = 0;
                int i = 0;
                @Override
                public int read() throws IOException {
                    if(len==0)
                    {
                        try {
                            while((len = receive())<=0)
                            {
                                Thread.sleep(1);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if(i==len)
                    {
                        len = 0;
                        i = 0;

                        return  -1;
                    }

                    return buffer[i++];
                }
            };

            outputStream = new OutputStream() {
                byte[] buffer = new byte[1];
                @Override
                public void write(int i) throws IOException {
                    buffer[0] = (byte) i;
                    send(buffer,1);
                }


            };

            listener.onConnected(inputStream,outputStream);


        } catch (SocketException e) {
            e.printStackTrace();
        }

        return  false;
    }

    private int receive() throws IOException {
        synchronized (this) {
            datagramSocket.receive(datagramPacket);
            address = datagramPacket.getAddress();
            return datagramPacket.getLength();
        }
    }

    private void send(byte[] buffer,int length) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, length,address,port);
        datagramSocket.send(datagramPacket);
    }

    public void close()
    {
        if(datagramSocket!=null)
        datagramSocket.close();

    }

    public interface UDPListener{
        void onConnected(InputStream inputStream,OutputStream outputStream);
    }
}
