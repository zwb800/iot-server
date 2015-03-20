package com.mobilejohnny.iotserver;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.mobilejohnny.iotserver.bluetooth.Bluetooth;
import com.mobilejohnny.iotserver.bluetooth.BluetoothListener;
import com.mobilejohnny.iotserver.bluetooth.TCP;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * Created by admin2 on 2015/3/20.
 */
public class TCPBluetoothTransferActivity extends ActionBarActivity {
    private Bluetooth bluetooth;
    private TCP tcp;
    private BluetoothListener bluetoothListener;
    private TCP.TCPListener tcpListener;
    private View decorView;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpbluetoothtransfer);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock =  powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"TCP_BT");
        decorView = getWindow().getDecorView();

        bluetoothListener = new BluetoothListener(){
            @Override
            public void result(final int result) {
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(TCPBluetoothTransferActivity.this, result == Bluetooth.RESULT_SUCCESS ? "成功" : "失败", Toast.LENGTH_SHORT).show();
//                    }
//                });

            }
        };

        tcpListener = new TCP.TCPListener() {
            @Override
            public void onReceive(char[] data) {
                bluetooth.send(new String(data));
            }
        };

        bluetooth = new Bluetooth(bluetoothListener);
        tcp = new TCP(tcpListener);

        bluetooth.connect("OFFICE");
        tcp.startServer(8080,tcpListener);
        wakeLock.acquire();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        tcp.close();
        bluetooth.close();
        wakeLock.release();
        super.onDestroy();
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
            {

            }

        }
    }

}
