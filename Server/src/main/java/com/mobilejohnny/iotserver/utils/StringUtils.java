package com.mobilejohnny.iotserver.utils;

/**
 * Created by admin2 on 2015/5/26.
 */
public class StringUtils {
    public static String convertToString(byte[] data) {
        StringBuffer stringBuffer = new StringBuffer(data.length*2);
        for (int i=0;i<data.length;i++)
        {

            stringBuffer.append( String.format("%x ",data[i]).toUpperCase());

        }
        return stringBuffer.toString();
    }

    public static String getIPString(int ip) {
        return String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
    }
}
