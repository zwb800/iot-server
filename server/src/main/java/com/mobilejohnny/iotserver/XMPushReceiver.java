package com.mobilejohnny.iotserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xiaomi.mipush.sdk.*;

/**
 * 1、PushMessageReceiver是个抽象类，该类继承了BroadcastReceiver。
 * 2、需要将自定义的DemoMessageReceiver注册在AndroidManifest.xml文件中 <receiver
 * android:exported="true"
 * android:name="com.xiaomi.mipushdemo.DemoMessageReceiver"> <intent-filter>
 * <action android:name="com.xiaomi.mipush.RECEIVE_MESSAGE" /> </intent-filter>
 * <intent-filter> <action android:name="com.xiaomi.mipush.ERROR" />
 * </intent-filter> </receiver>
 * 3、DemoMessageReceiver的onCommandResult方法用来接收客户端向服务器发送命令后的响应结果
 * 4、DemoMessageReceiver的onReceiveMessage方法用来接收服务器向客户端发送的消息
 * 5、onReceiveMessage和onCommandResult方法运行在非UI线程中
 * 
 * @author wangkuiwei
 */
public class XMPushReceiver extends PushMessageReceiver {

    public static final String ACTION_SEND = "com.mobilejohnny.iotserver.action.SEND";
    public static final String EXTRA_PARAM_MESSAGE = "com.mobilejohnny.iotserver.extra.PARAM_MESSAGE";

    @Override
    public void onReceiveMessage(Context context, MiPushMessage message) {
        Log.d(XMActivity.TAG,
                "onReceiveMessage is called. " + message.toString());
        String msg = message.getContent();

        Intent i = new Intent(ACTION_SEND);
        i.putExtra(EXTRA_PARAM_MESSAGE,msg);

        BluetoothService.startActionSend(context,msg);
        context.sendBroadcast(i);

//        String log = context.getString(R.string.recv_message, message.getContent());
//        MainActivity.logList.add(0, getSimpleDate() + " " + log);

//        Message msg = Message.obtain();
//        if (message.isNotified()) {
////            msg.obj = log;
//        }
//        DemoApplication.getHandler().sendMessage(msg);
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        Log.d(XMActivity.TAG,
                "onCommandResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
//        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
//        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        String log = "";

        if(message.getResultCode()== ErrorCode.SUCCESS)
        {
            if(MiPushClient.COMMAND_REGISTER.equals(command))
            {
                String reg_id = arguments.get(0);

                Log.d(XMActivity.TAG,"RegID:"+reg_id);
            }
        }

//        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
//            if (message.getResultCode() == ErrorCode.SUCCESS) {
//                log = context.getString(R.string.register_success);
//            } else {
//                log = context.getString(R.string.register_fail);
//            }
//        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
//            if (message.getResultCode() == ErrorCode.SUCCESS) {
//                log = context.getString(R.string.set_alias_success, cmdArg1);
//            } else {
//                log = context.getString(R.string.set_alias_fail, message.getReason());
//            }
//        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
//            if (message.getResultCode() == ErrorCode.SUCCESS) {
//                log = context.getString(R.string.unset_alias_success, cmdArg1);
//            } else {
//                log = context.getString(R.string.unset_alias_fail, message.getReason());
//            }
//        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
//            if (message.getResultCode() == ErrorCode.SUCCESS) {
//                log = context.getString(R.string.subscribe_topic_success, cmdArg1);
//            } else {
//                log = context.getString(R.string.subscribe_topic_fail, message.getReason());
//            }
//        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
//            if (message.getResultCode() == ErrorCode.SUCCESS) {
//                log = context.getString(R.string.unsubscribe_topic_success, cmdArg1);
//            } else {
//                log = context.getString(R.string.unsubscribe_topic_fail, message.getReason());
//            }
//        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
//            if (message.getResultCode() == ErrorCode.SUCCESS) {
//                log = context.getString(R.string.set_accept_time_success, cmdArg1, cmdArg2);
//
//                Message msg = Message.obtain();
//                msg.what = 1;
//                msg.arg1 = (cmdArg1.equals(cmdArg2))?0:1;
//                DemoApplication.getHandler().sendMessage(msg);
//            } else {
//                log = context.getString(R.string.set_accept_time_fail, message.getReason());
//            }
//        } else {
//            log = message.getReason();
//        }
//        MainActivity.logList.add(0, getSimpleDate() + "    " + log);
//
//        Message msg = Message.obtain();
//        msg.obj = log;
//        DemoApplication.getHandler().sendMessage(msg);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getSimpleDate() {
        return new SimpleDateFormat("MM-dd hh:mm:ss").format(new Date());
    }

    public static class DemoHandler extends Handler {

        private Context context;

        public DemoHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            String s = (String) msg.obj;
            
//            if (MainActivity.sMainActivity != null) {
//                MainActivity.sMainActivity.refreshLogInfo();
//
//                if (msg.what==1) {
//                    MainActivity.sMainActivity.refreshPushState((msg.arg1==1)?true:false);
//                }
//            }
            if (!TextUtils.isEmpty(s)) {
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }
        }
    }
}
