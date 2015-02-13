package com.mobilejohnny.iotwidget;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Sender;
import org.json.simple.parser.ParseException;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private static final String APP_SECRET = "TOrA9OlkZYhMysgsHtmT5w==";
    private static final String APP_PACKAGE = "com.mobilejohnny.iotserver" ;
    private Button btnSend;
    private EditText txtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtMessage = (EditText)findViewById(R.id.txt_message);
        btnSend = (Button)findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(txtMessage.getText().toString());
            }
        });


    }

    private void sendMessage2(String msg)
    {

    }

    private void sendMessage(String msg)
    {
        Constants.useOfficial();
        final Sender sender = new Sender(APP_SECRET);
        final Message message = new Message.Builder()
                .payload(msg)
                .passThrough(1)
                .restrictedPackageName(APP_PACKAGE)
                .build();
        Log.i("mipush","开始发送");
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean result = false;
                try {
                    sender.broadcastAllNoRetry(message);
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("",e.getMessage());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                super.onPostExecute(b);
                Toast.makeText(MainActivity.this, "发送"+(b?"成功":"失败"),Toast.LENGTH_SHORT).show();

            }
        }.execute();
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
        return super.onOptionsItemSelected(item);
    }
}
