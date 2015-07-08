package com.mobilejohnny.iotwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


public class ColorSelectDialogBuilder  {

    public static final String EXTRA_COLOR = "color";

    public static final int[] colors = new int[]{
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
    private final Activity context;
    public GridView colorSelectList;
    private DialogInterface.OnClickListener listener;
    private AlertDialog alertDialog;

    public ColorSelectDialogBuilder(Activity context)
    {
        this.context = context;
    }

    public void  setOnItemClickListener (DialogInterface.OnClickListener listener)
    {
        this.listener = listener;
    }

    public AlertDialog build() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        View view = context.getLayoutInflater().inflate(R.layout.activity_color_select_dialog, null);


        colorSelectList = (GridView) view.findViewById(R.id.colorSelectList);

        colorSelectList.setAdapter(adapter);
        colorSelectList.setOnItemClickListener(onItemClick);

        alertDialog = alertDialogBuilder.setView(view).create();
        return alertDialog ;
    }

    private ListAdapter adapter = new BaseAdapter() {
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
            if (view == null) {
                view = context.getLayoutInflater().inflate(R.layout.color_item, viewGroup, false);
            }

            view.setBackgroundColor(context.getResources().getColor(getItem(i)));
            return view;
        }
    };

    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
             int color = (int) adapterView.getItemAtPosition(i);

            listener.onClick(alertDialog,color);

        }
    };
}
