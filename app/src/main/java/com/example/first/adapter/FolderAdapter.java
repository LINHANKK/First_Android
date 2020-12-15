package com.example.first.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.first.R;
import com.example.first.base.AppInfo;

import java.util.List;

public class FolderAdapter extends BaseAdapter {
    List<AppInfo> Apps;
    Context context;

    public FolderAdapter(Context context, List<AppInfo> Apps) {
        this.Apps = Apps;
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_choose, null);
            convertView.setLayoutParams(new GridView.LayoutParams(220, 280));
            //viewHolder.iv = new ImageView(MainActivity.this);
            //viewHolder.iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //viewHolder.iv.setLayoutParams(new GridView.LayoutParams(150, 150));

            viewHolder.cb = convertView.findViewById(R.id.cb);
            viewHolder.iv = convertView.findViewById(R.id.item_iv);
            viewHolder.tv = convertView.findViewById(R.id.item_tv);



            convertView.setTag(viewHolder);
        } else {
            //viewHolder.iv = (ImageView) convertView;
            viewHolder = (ViewHolder) convertView.getTag();
            convertView.setTag(viewHolder);
        }

        AppInfo info = Apps.get(position);
        if(info.getWhere().equals("desk")){
            viewHolder.cb.setChecked(false);
        }else{
            viewHolder.cb.setChecked(true);
        }
        viewHolder.iv.setImageDrawable(info.getAppIcon());
        viewHolder.tv.setText(info.getAppName());

        return convertView;
    }


    public final int getCount() {
        return Apps.size();
    }

    public final Object getItem(int position) {
        return Apps.get(position);
    }

    public final long getItemId(int position) {
        return position;
    }

    public class ViewHolder{
        CheckBox cb;
        ImageView iv;
        TextView tv;
    }
}
