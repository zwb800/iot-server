package com.mobilejohnny.iotclient;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

/**
 * Created by zwb on 15-8-13.
 */
public class Push {
    private static final String APP_KEY = "55c8c6339477ebf5246956dc";

    public static void connect (final PushCallback callback)
    {
        final SocketIO socketIO = new SocketIO();
        try {
            socketIO.connect("http://sock.yunba.io:3000", new IOCallback() {
                @Override
                public void onDisconnect() {

                }

                @Override
                public void onConnect() {

                }

                @Override
                public void onMessage(String s, IOAcknowledge ioAcknowledge) {
                    System.out.println(s);
                }

                @Override
                public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {
                    try {
                        System.out.println(jsonObject.getString("msg"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void on(String event, IOAcknowledge ioAcknowledge, Object... args) {
                    try {
                        if(event.equals("socketconnectack"))
                        {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("appkey",APP_KEY);
                            jsonObject.put("customid","1");

                            socketIO.emit("connect",jsonObject);
                        }
                        else if(event.equals("connack"))
                        {
                            JSONObject jsonObject = (JSONObject)args[0];
                            String sessionid = jsonObject.getString("sessionid");
                            System.out.println(sessionid);
                            JSONObject jsonAlias = new JSONObject();
                            jsonAlias.put("alias","1");
                            socketIO.emit("set_alias",jsonAlias);
                        }
                        else if(event.equals("set_alias_ack"))
                        {
                            JSONObject jsonObject = (JSONObject)args[0];
                            boolean success = jsonObject.getBoolean("success");
                            System.out.println("set alias "+(success?"success":"failed"));
                        }
                        else if(event.equals("message"))
                        {
                            JSONObject jsonObject = (JSONObject)args[0];
                            String msg = jsonObject.getString("msg");
                            callback.onMessage(msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(SocketIOException e) {

                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
