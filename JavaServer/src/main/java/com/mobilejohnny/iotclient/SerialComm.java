package com.mobilejohnny.iotclient;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zwb on 15-8-4.
 */
public class SerialComm {

    private static InputStream inputStream;
    private static OutputStream outputStream;

    public static boolean connect(String portName)
    {
        boolean result = false;
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

            if(portIdentifier.isCurrentlyOwned())
            {
                throw new PortInUseException();
            }
            else
            {
                int timeout = 2000;
                CommPort port = portIdentifier.open(SerialComm.class.getName(),timeout);
                if(port!=null)
                {
                    SerialPort serialPort = (SerialPort)port;
                    int baudRate = 115200;
                    int bit = SerialPort.DATABITS_8;
                    int stopBit = SerialPort.STOPBITS_1;
                    int parity = SerialPort.PARITY_NONE;//奇偶校验

                    serialPort.setSerialPortParams(baudRate,bit,stopBit,parity);
                    inputStream = serialPort.getInputStream();
                    outputStream = serialPort.getOutputStream();
                    result = true;
                }
                else
                {
                    System.out.println("Error:Only serial ports are handled");
                }
            }
        } catch (NoSuchPortException e) {
            e.printStackTrace();
            System.out.println("Error:Port not found or no permission");
        } catch (PortInUseException e) {
//            e.printStackTrace();
            System.out.println("Error:Port is currently in use");
        } catch (UnsupportedCommOperationException e) {
//            e.printStackTrace();
            System.out.println("Error:Wrong port settings");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

    public static InputStream getInputStream() {
        return inputStream;
    }

    public static OutputStream getOutputStream() {
        return outputStream;
    }
}
