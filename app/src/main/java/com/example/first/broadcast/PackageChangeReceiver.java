package com.example.first.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageChangeReceiver extends BroadcastReceiver {
    private PackageChangeListener listener;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener != null){
            listener.packageChange();
        }
    }

    public interface PackageChangeListener  {
        void packageChange();
    }

    public void OnPackageChangeListener(PackageChangeListener listener){
        this.listener =  listener;
    }
}
