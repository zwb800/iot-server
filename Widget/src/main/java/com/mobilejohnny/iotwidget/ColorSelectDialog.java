package com.mobilejohnny.iotwidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


public class ColorSelectDialog extends Activity {


    public static final String EXTRA_COLOR = "color";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_select_dialog);
        setResult(RESULT_CANCELED);

        final GridView colorSelectList = (GridView) findViewById(R.id.colorSelectList);


        final int[] colors = new int[]{
                R.color.user_icon_1,
                R.color.user_icon_2,
                R.color.user_icon_3,
                R.color.user_icon_4,
                R.color.user_icon_5,
                R.color.user_icon_6,
                R.color.user_icon_7,
                R.color.user_icon_8,
                R.color.user_icon_default_gray,
                R.color.user_icon_default_white,
        };

        colorSelectList.setOnItemClickListener(onItemClick);

        colorSelectList.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return colors.length;
            }

            @Override
            public Integer getItem(int i) {
                return colors[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view==null)
                {
                    view = getLayoutInflater().inflate(R.layout.color_item,viewGroup,false);
                }

                view.setBackgroundColor(getResources().getColor(getItem(i)));
                return view;
            }
        });
    }

    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = getIntent();
            intent.putExtra(EXTRA_COLOR,(int)adapterView.getItemAtPosition(i));
            setResult(RESULT_OK, intent);
            finish();
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_color_select_dialog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
