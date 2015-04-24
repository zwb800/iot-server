package com.mobilejohnny.iotserver;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by admin on 2015/4/23.
 */
public class MSP {

    private static final String MSP_HEADER = "$M<";
    private static final byte[] MSP_HEADER_BYTE = MSP_HEADER.getBytes();
    private static final int headerLength = MSP_HEADER_BYTE.length;

    static final int minRC = 1000, maxRC = 2000;
    //, medRC = 1500;
    protected static int medRollRC = 1500,medPitchRC = 1500,medYawRC = 1500;

    static int servo[] = new int[8],
            rcThrottle = minRC, rcRoll = medRollRC, rcPitch = medPitchRC, rcYaw =medYawRC,
            rcAUX1=minRC, rcAUX2=minRC, rcAUX3=minRC, rcAUX4=minRC;

    public static final int//这里用的int 当转换成byte时因为java的补码应该是负数 传入c语言中的无符号字节正常

            MSP_SET_RAW_RC  = 200,
            MSP_SET_RAW_GPS = 201;
    static byte[] payloadChar = new byte[16];

    public void updateRCPayload() {
        payloadChar[0] = (byte)(rcRoll & 0xFF); //strip the 'most significant bit' (MSB) and buffer\
        payloadChar[1] = (byte)(rcRoll >> 8 & 0xFF); //move the MSB to LSB, strip the MSB and buffer
        payloadChar[2] = (byte)(rcPitch & 0xFF);//先传低8位
        payloadChar[3] = (byte)(rcPitch >> 8 & 0xFF);//再传高8位
        payloadChar[4] = (byte)(rcYaw & 0xFF);
        payloadChar[5] = (byte)(rcYaw >> 8 & 0xFF);
        payloadChar[6] = (byte)(rcThrottle & 0xFF);
        payloadChar[7] = (byte)(rcThrottle >> 8 & 0xFF);

        //aux1
        payloadChar[8] = (byte)(rcAUX1 & 0xFF);
        payloadChar[9] = (byte)(rcAUX1 >> 8 & 0xFF);

        //aux2
        payloadChar[10] = (byte)(rcAUX2 & 0xFF);
        payloadChar[11] = (byte)(rcAUX2 >> 8 & 0xFF);

        //aux3
        payloadChar[12] = (byte)(rcAUX3 & 0xFF);
        payloadChar[13] = (byte)(rcAUX3 >> 8 & 0xFF);

        //aux4
        payloadChar[14] = (byte)(rcAUX4 & 0xFF);
        payloadChar[15] = (byte)(rcAUX4 >> 8 & 0xFF);
    }

    private static byte[] gpsData = new byte[14];
    public void updateGPS(byte fix,byte numSat,int longitude,int latitude,int altitude,int speed)
    {
        gpsData[0] = fix;
        gpsData[1] = numSat;
        gpsData[2] = (byte) latitude;
        gpsData[3] = (byte) (latitude >> 8);
        gpsData[4] = (byte) (latitude >> 16);
        gpsData[5] = (byte) (latitude >> 32);
        gpsData[6] = (byte) longitude;
        gpsData[7] = (byte) (longitude >> 8);
        gpsData[8] = (byte) (longitude >> 16);
        gpsData[9] = (byte) (longitude >> 32);
        gpsData[10] = (byte) altitude;
        gpsData[11] = (byte) (altitude >> 8);
        gpsData[12] = (byte) speed;
        gpsData[13] = (byte) (speed >>8);
    }

    public byte[] getMSP_GPS()
    {
        return requestMSP(MSP_SET_RAW_GPS, gpsData);
    }

    static private int irmsp_RC =0;
    static private final int mspLenght_RC = 22;
    static private int bRMSP_RC=0;
    private static byte[] msp_RC;
    public void sendRCPayload() {
        byte[] arr_RC;
        irmsp_RC =0;
        bRMSP_RC=0;
        arr_RC = new byte[mspLenght_RC];
        msp_RC = requestMSP(MSP_SET_RAW_RC, payloadChar );

        try {
            for (bRMSP_RC=0;bRMSP_RC<mspLenght_RC;bRMSP_RC++) {
                arr_RC[irmsp_RC++] = msp_RC[bRMSP_RC];
            }
//            send(arr_RC);
        }
        catch(NullPointerException ex) {
            Log.e("", "Warning: Packet not sended.");
        }
    }

    //send msp with payload
//    The general format of an MSP message is:
//    <preamble>,<direction>,<size>,<command>,<data>,<crc>
//    Where:
//    preamble = the ASCII characters '$M'
//    direction = the ASCII character '<' if to the MWC or '>' if from the MWC
//    size = number of data bytes, binary. Can be zero as in the case of a data request to the MWC
//    command = message_id as per the table below
//    data = as per the table below. UINT16 values are LSB first.
//    crc = XOR of <size>, <command> and each data byte into a zero'ed sum
    private byte[] requestMSP (int msp, byte[] payload) {
        byte[] bf;
        int cList;
        byte checksumMSP;
        byte pl_size;
        int cMSP;
        int payloadLength;

        if (msp < 0) {
            return null;
        }
        
        pl_size = (byte)((payload != null ? parseInt(payload.length) : 0)&0xFF);
        
        bf = new byte[headerLength+pl_size+3];
        int i = 0;
        for (cList=0;cList<headerLength;cList++) {//加入头字节$M<
            bf[i++] = MSP_HEADER_BYTE[cList] ;
        }

        checksumMSP=0;
       
        bf[i++] = pl_size;//加入长度
        checksumMSP ^= (pl_size&0xFF);

        bf[i++] = (byte)(msp & 0xFF);//加入命令
        checksumMSP ^= (msp&0xFF);

        if (payload != null) {
            payloadLength = payload.length;
            for (cMSP=0;cMSP<payloadLength;cMSP++) {
                bf[i++] = (byte)(payload[cMSP]&0xFF);//加入数据
                checksumMSP ^= (payload[cMSP]&0xFF);
            }
        }
        bf[i++] = checksumMSP;
        
        return bf;
    }

    private char parseChar(int val)
    {
        return (char)val;
    }

    private int parseInt(float val)
    {
        return (int)val;
    }
}
