package com.mobilejohnny.iotwidget.utils;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by admin2 on 2015/7/17.
 */
public class Request {
    public static String post(String url,ArrayList<NameValuePair> parameters)
    {
        String result = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        try {
            post.setEntity(new UrlEncodedFormEntity(parameters));

            HttpResponse response = httpClient.execute(post);
            result= EntityUtils.toString(response.getEntity());
            Log.i("Request", response.getStatusLine().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
