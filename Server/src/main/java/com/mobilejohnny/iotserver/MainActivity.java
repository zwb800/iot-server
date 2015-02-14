package com.mobilejohnny.iotserver;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.*;


public class MainActivity extends ActionBarActivity  {

    public static final String ACTION_SEND = "com.mobilejohnny.iotserver.action.SEND";
    public static final String EXTRA_PARAM_MESSAGE = "com.mobilejohnny.iotserver.extra.PARAM_MESSAGE";
    public static final String ACTION_XMPUSH_REGISTER = "com.mobilejohnny.iotserver.action.XMPUSH_REGISTER";

    // user your appid the key.
    public static final String APP_ID = "2882303761517303294";
    // user your appid the key.
    public static final String APP_KEY = "5161730349294";

    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep
    // com.xiaomi.mipushdemo
    public static final String TAG = "xmpush";

    private TextView txtData;



    Receiver receiver = null;
    private Button btnConnect;

    private Handler handler = null;
    private TextView txtRegID;
    private TextView txtBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothService.startActionConnect(this);

        txtData = (TextView)findViewById(R.id.txt_data);
        txtRegID = (TextView)findViewById(R.id.txt_regid);
        txtBluetooth = (TextView)findViewById(R.id.txt_bluetooth);
        btnConnect = (Button)findViewById(R.id.btn_connect);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("message");
                txtData.setText(message);
            }
        };

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               BluetoothService.startActionSend(MainActivity.this,txtData.getText().toString());
            }
        });



        MiPushClient.registerPush(this, APP_ID, APP_KEY);

        Log.d(TAG,"开始注册...");

        setLogger();
    }



    private void setLogger() {
        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };
        Logger.setLogger(this, newLogger);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        else if (id == R.id.action_exit) {
            BluetoothService.startActionDisconnect(this);
            MiPushClient.unregisterPush(this);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiver = new Receiver();
        registerReceiver(receiver, new IntentFilter(ACTION_SEND));
        registerReceiver(receiver, new IntentFilter(ACTION_XMPUSH_REGISTER));
    }

    @Override
    protected void onStop() {
        unregisterReceiver(receiver);

        receiver = null;
        handler = null;
        super.onStop();
    }

    public static void startActionXMPUSH_REGISTER(Context context,String reg_id)
    {
        Intent i = new Intent(ACTION_XMPUSH_REGISTER);
        i.putExtra(EXTRA_PARAM_MESSAGE, reg_id);
        context.sendBroadcast(i);
    }

    public static void startActionMessage(Context context,String message)
    {
        Intent i = new Intent(ACTION_SEND);
        i.putExtra(EXTRA_PARAM_MESSAGE, message);
        context.sendBroadcast(i);
    }

    private class Receiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(ACTION_SEND)) {
                Message message1 = new Message();
                message1.getData().putString("message", intent.getStringExtra(EXTRA_PARAM_MESSAGE));
                handler.sendMessage(message1);
            }
            else if(intent.getAction().equals(ACTION_XMPUSH_REGISTER)) {
                final String regid = intent.getStringExtra(EXTRA_PARAM_MESSAGE);
                new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        txtRegID.setText(regid);
                    }
                }.sendEmptyMessage(0);
            }
        }
    }
}
