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
    static Character[] payloadChar = new Character[16];

    public void updateRCPayload() {
        payloadChar[0] = parseChar(rcRoll & 0xFF); //strip the 'most significant bit' (MSB) and buffer\
        payloadChar[1] = parseChar(rcRoll >> 8 & 0xFF); //move the MSB to LSB, strip the MSB and buffer
        payloadChar[2] = parseChar(rcPitch & 0xFF);//先传低8位
        payloadChar[3] = parseChar(rcPitch >> 8 & 0xFF);//再传高8位
        payloadChar[4] = parseChar(rcYaw & 0xFF);
        payloadChar[5] = parseChar(rcYaw >> 8 & 0xFF);
        payloadChar[6] = parseChar(rcThrottle & 0xFF);
        payloadChar[7] = parseChar(rcThrottle >> 8 & 0xFF);

        //aux1
        payloadChar[8] = parseChar(rcAUX1 & 0xFF);
        payloadChar[9] = parseChar(rcAUX1 >> 8 & 0xFF);

        //aux2
        payloadChar[10] = parseChar(rcAUX2 & 0xFF);
        payloadChar[11] = parseChar(rcAUX2 >> 8 & 0xFF);

        //aux3
        payloadChar[12] = parseChar(rcAUX3 & 0xFF);
        payloadChar[13] = parseChar(rcAUX3 >> 8 & 0xFF);

        //aux4
        payloadChar[14] = parseChar(rcAUX4 & 0xFF);
        payloadChar[15] = parseChar(rcAUX4 >> 8 & 0xFF);
    }

    static private int irmsp_RC =0;
    static private final int mspLenght_RC = 22;
    static private int bRMSP_RC=0;
    private static List<Byte> msp_RC;
    public void sendRCPayload() {
        byte[] arr_RC;
        irmsp_RC =0;
        bRMSP_RC=0;
        arr_RC = new byte[mspLenght_RC];
        msp_RC = requestMSP(MSP_SET_RAW_RC, payloadChar );

        try {
            for (bRMSP_RC=0;bRMSP_RC<mspLenght_RC;bRMSP_RC++) {
                arr_RC[irmsp_RC++] = msp_RC.get(bRMSP_RC);
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
    private List<Byte> requestMSP (int msp, Character[] payload) {
        List<Byte> bf;
        int cList;
        byte checksumMSP;
        byte pl_size;
        int cMSP;
        int payloadLength;

        if (msp < 0) {
            return null;
        }
        bf = new LinkedList<Byte>();
        for (cList=0;cList<headerLength;cList++) {//加入头字节$M<
            bf.add( MSP_HEADER_BYTE[cList] );
        }

        checksumMSP=0;
        pl_size = (byte)((payload != null ? parseInt(payload.length) : 0)&0xFF);
        bf.add(pl_size);//加入长度
        checksumMSP ^= (pl_size&0xFF);

        bf.add((byte)(msp & 0xFF));//加入命令
        checksumMSP ^= (msp&0xFF);

        if (payload != null) {
            payloadLength = payload.length;
            for (cMSP=0;cMSP<payloadLength;cMSP++) {
                bf.add((byte)(payload[cMSP]&0xFF));//加入数据
                checksumMSP ^= (payload[cMSP]&0xFF);
            }
        }
        bf.add(checksumMSP);
        return (bf);
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
