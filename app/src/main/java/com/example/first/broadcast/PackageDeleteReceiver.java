package com.example.first.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 监听应用的删除操作
 */
public class PackageDeleteReceiver extends BroadcastReceiver {
    private PackageDeleteListener listener;
    @Override
    public void onReceive(Context context, Intent intent) {

        if (listener != null){
            listener.packageDelete();
        }

    }

    //接口  方便操作
    public interface PackageDeleteListener  {
        void packageDelete();
    }

    public void OnPackageDeleteListener(PackageDeleteListener listener){
        this.listener =  listener;
    }
}
