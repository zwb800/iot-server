package com.mobilejohnny.iotserver;

import android.hardware.usb.*;

/**
 * Created by admin2 on 2015/4/11.
 */
public class FDTI {
    private static final int REQUEST_TYPE_OUT = 0x40;
    private static final int REQUEST_RESET = 0;
    private static final int REQUEST_SET_BUADRATE = 3;

    private UsbManager manager;
    private UsbDevice device;
    private int bcdDevice = 1;//FT232RL
    private int numOfChannels = 6;
    private UsbEndpoint endpointIN;
    private UsbEndpoint endpointOUT;
    private UsbDeviceConnection connection;

    public FDTI(UsbManager manager,UsbDevice device)
    {
        this.manager = manager;
        this.device = device;
    }

    public void begin() {
        UsbInterface usbInterface = device.getInterface(0);
        connection = manager.openDevice(device);

        if(connection.claimInterface(usbInterface,true))
        {
            reset();
            clear();
            setBaudRate();

            for (int i=0;i<usbInterface.getEndpointCount();i++)
            {
                UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                {
                    if(endpoint.getDirection() == UsbConstants.USB_DIR_IN)
                    {
                        endpointIN = endpoint;
                    }
                    else
                    {
                        endpointOUT = endpoint;
                    }
                }
            }
        }
    }

    public int reset() {
        return connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_RESET,0,0,null,0,0);
    }

    public int clear() {
        connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_RESET,1,0,null,0,0);//clear Rx
        return connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_RESET,2,0,null,0,0);//clear Tx
    }

    public void setBaudRate()
    {
        int rate = 0x001a;//115200
        connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_SET_BUADRATE,rate,0,null,0,0);
    }

    public int write(byte[] data)
    {
        return connection.bulkTransfer(endpointOUT,data,data.length,0);
    }

    public int read(byte[] data)
    {
        return connection.bulkTransfer(endpointIN,data,data.length,0);
    }

    public void close()
    {
        connection.close();
    }
}
