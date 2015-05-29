package com.mobilejohnny.iotserver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class GcmActivity extends ActionBarActivity {
    private static final String TAG = "Iot Server";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 0;
    private static final String SENDER_ID = "949096630918";
    private GoogleCloudMessaging gcm;
    private String regid;
    private TextView txt;
    private Button btnRegister;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcm);

        txt = (TextView) findViewById(R.id.txt);
        btnRegister = (Button)findViewById(R.id.btn_register);
        progressDialog = new ProgressDialog(GcmActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("注册中...");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerInBackground();
            }
        });

        if(checkPlayServices())
        {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this);

            if(regid==null||regid.equals(""))
            {
                registerInBackground();
            }
        }
    }

    private String getRegistrationId(Context context) {
        return null;
    }

    private void registerInBackground()
    {
        progressDialog.show();
        txt.setText(null);
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected String doInBackground(Object[] objects) {
                String msg = "";
                if (gcm == null)
                    gcm = GoogleCloudMessaging.getInstance(GcmActivity.this);

                try {
                    regid = gcm.register(SENDER_ID);
                    msg = regid;
                } catch (IOException e) {
                    msg = e.getMessage();
                }

                return msg;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                txt.setText(o.toString());
                progressDialog.dismiss();
            }
        };

        asyncTask.execute(null,null,null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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
