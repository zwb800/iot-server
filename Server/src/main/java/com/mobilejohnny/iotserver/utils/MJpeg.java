package com.mobilejohnny.iotserver.utils;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Created by admin2 on 2015/5/14.
 */
public class MJpeg {

    public static byte[] HEADER_MJPG = (
            "Connection: Close" +
                    "\r\n" +
                    "Server: Test" +
                    "\r\n" +
                    "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0"+
                    "\r\n" +
                    "Pragma: no-cache"+
                    "\r\n"+
                    "Content-type:multipart/x-mixed-replace;boundary=7b3cc56e5f51db803f790dad720ed50a" +
                    "Date: "+new Date().toString()+
                    "Last Modified: "+new Date().toString()
    ).getBytes();

    protected static byte[] HEADER_JPG = (
            "\r\n" +
            "--7b3cc56e5f51db803f790dad720ed50a" +
            "\r\n" +
            "Content-type: image/jpeg" +
            "\r\n" +
            "\r\n"
    ).getBytes();

    private OutputStream outputStream;

    public MJpeg(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

    public void sendFrame(Bitmap frame) throws IOException {
        outputStream.write(HEADER_JPG);
        frame.compress(Bitmap.CompressFormat.JPEG,60,outputStream);
    }
}
