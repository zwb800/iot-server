package com.mobilejohnny.iotserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.mobilejohnny.iotserver.R;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.*;

import java.util.List;


public class XMActivity extends ActionBarActivity  {

    // user your appid the key.
    public static final String APP_ID = "2882303761517303294";
    // user your appid the key.
    public static final String APP_KEY = "5161730349294";

    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep
    // com.xiaomi.mipushdemo
    public static final String TAG = "com.xiaomi.mipushdemo";

    private TextView txt;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            txt.setText(msg.getData().getString("reg_id"));
        }
    };

    Receiver receiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xm);

        txt = (TextView)findViewById(R.id.txt1);

        receiver = new Receiver();



//        LoggerInterface newLogger = new LoggerInterface() {
//
//            @Override
//            public void setTag(String tag) {
//                // ignore
//            }
//
//            @Override
//            public void log(String content, Throwable t) {
//                Log.d(TAG, content, t);
//            }
//
//            @Override
//            public void log(String content) {
//                Log.d(TAG, content);
//            }
//        };
//        Logger.setLogger(this, newLogger);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.xm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        registerReceiver(receiver, new IntentFilter("com.xiaomi.mipush.RECEIVE_MESSAGE"));
        registerReceiver(receiver, new IntentFilter("com.xiaomi.mipush.ERROR"));
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        MiPushClient.registerPush(this, APP_ID, APP_KEY);
        Log.d(TAG,"开始注册...");


        super.onResume();

    }

    @Override
    protected void onStop() {
        unregisterReceiver(receiver);

        super.onStop();

    }

    private class Receiver extends PushMessageReceiver{

        @Override
        public void onReceiveMessage(Context context, MiPushMessage miPushMessage) {
            Log.d(TAG,"onReceiveMessage is called.");
        }

        @Override
        public void onCommandResult(Context context, MiPushCommandMessage message) {
            Log.d(XMActivity.TAG,
                    "onCommandResult is called. " + message.toString());
            String command = message.getCommand();
            List<String> arguments = message.getCommandArguments();

            if(message.getResultCode()== ErrorCode.SUCCESS)
            {
                if(MiPushClient.COMMAND_REGISTER.equals(command))
                {
                    String reg_id = arguments.get(0);
                    Message message1 = new Message();
                    message1.getData().putString("reg_id",reg_id);
                    handler.sendMessage(message1);
                }
            }
        }
    }
}
