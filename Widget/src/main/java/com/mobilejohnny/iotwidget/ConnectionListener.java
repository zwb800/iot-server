package com.mobilejohnny.iotwidget;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by admin on 2015/4/13.
 */
public interface ConnectionListener {
    public static final int RESULT_DEVICE_NOTFOUND = 1;
    public static final int RESULT_FAILD = 2;
    public static final int RESULT_SUCCESS = 3;
    public static final int RESULT_BLUETOOTH_DISABLED = 4;

    void result(int result, InputStream in, OutputStream out);
}
