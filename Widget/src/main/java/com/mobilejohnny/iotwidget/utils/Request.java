package com.mobilejohnny.iotwidget.utils;

import android.util.Log;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by admin2 on 2015/7/17.
 */
public class Request {
    public static String post(String strurl)
    {
        String result = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(strurl);
            connection = (HttpURLConnection)url.openConnection();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            byte[] buffer = new byte[1024*64];
            int len = in.read(buffer,0,buffer.length);
            result = new String(buffer,0,len);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection!=null)
                connection.disconnect();
        }

        return  result;
    }
}
