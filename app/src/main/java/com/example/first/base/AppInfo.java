package com.example.first.base;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.litepal.crud.DataSupport;

import java.io.InputStream;
import java.io.Serializable;

public class AppInfo extends DataSupport implements Serializable {
    private String appName;
    private String packageName;
    private String cls;
    private Drawable appIcon;
    private boolean folder;    //是否是文件夹
    private String where;    //显示位置（桌面or某个文件夹）
    private String imgStr;      //drawable转为string的存储变量

    public InputStream getiS() {
        return iS;
    }

    public void setiS(InputStream iS) {
        this.iS = iS;
    }

    private InputStream iS;     //drawable转为InputStream的存储变量

    public AppInfo(){}

    public AppInfo(String appName, String packageName, String cls, Drawable appIcon, boolean folder, String where){
        this.appName = appName;
        this.packageName = packageName;
        this.cls = cls;
        this.appIcon = appIcon;
        this.folder = folder;
        this.where = where;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
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

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getImgStr() {
        return imgStr;
    }

    public void setImgStr(String imgStr) {
        this.imgStr = imgStr;
    }
}
