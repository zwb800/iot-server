package com.mobilejohnny.iotclient;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

/*×

 */
public class Main {


//http://rest.yunba.io:8080/?method=publish_to_alias&appkey=55c8c6339477ebf5246956dc&seckey=sec-smA1Ze8y1pZC0qAELDbrjuDnHAaJvMeutk4L6WTU8YQU0ewc&alias=1&msg=111
    public static void main(String[] args) {
        System.out.println("Starting...");
        boolean result = SerialComm.connect("COM11");

        if(result)
        {
            final OutputStream outputStream = SerialComm.getOutputStream();

            Push.connect(new PushCallback() {
                @Override
                public void onMessage(String msg) {
                    try {
                        outputStream.write((msg).getBytes());
                        System.out.println(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(120000);
//                        System.exit(0);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
        }
    }
}
