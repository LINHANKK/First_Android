package com.example.first.base;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class AppInfo implements Serializable {
    private String appName;
    private String packageName;
    private Drawable appIcon;
    private String Cls;

    public AppInfo(){
    }

    public AppInfo(String appName, String packageName, Drawable appIcon, String Cls) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.Cls = Cls;
    }

    public String getCls() {
        return Cls;
    }

    public void setCls(String cls) {
        Cls = cls;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
