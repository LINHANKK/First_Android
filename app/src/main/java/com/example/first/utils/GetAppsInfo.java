package com.example.first.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;


import com.example.first.MainActivity;
import com.example.first.base.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class GetAppsInfo {

    private PackageManager packageManager;
    private int mIconDpi;
    private ArrayList<AppInfo> appInfos = new ArrayList<AppInfo>();

    public GetAppsInfo(Context mContext){
        ActivityManager activityManager =
                (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = mContext.getPackageManager();
        mIconDpi = activityManager.getLauncherLargeIconDensity();
    }

    private void loadAppsInfo() {
        List<ResolveInfo> apps = null;
        Intent filterIntent = new Intent(Intent.ACTION_MAIN, null);
        //Intent.CATEGORY_LAUNCHER主要的过滤条件
        filterIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        //获取ResolveInfo列表
        apps = packageManager.queryIntentActivities(filterIntent, 0);
        for (ResolveInfo resolveInfo : apps){
            AppInfo appInfo = new AppInfo();

            appInfo.setPackageName(resolveInfo.activityInfo.applicationInfo.packageName);    //获取包名
            appInfo.setAppName(resolveInfo.loadLabel(packageManager).toString());    //获取应用名
            appInfo.setCls(resolveInfo.activityInfo.name);    //只对app有意义，获取主类
            appInfo.setAppIcon(getResIconFormActyInfo(resolveInfo.activityInfo));    //获取应用图标（后期要优化）
            appInfo.setFolder(false);    //初始默认不是文件夹（后期要优化）
            appInfo.setWhere("desk");    //初始默认显示位置为桌面（后期要优化）

            appInfos.add(appInfo);
        }
    }

    private void loadFolderInfo(String folderName) {

        //遍历appInfos，寻找
        for (AppInfo folderInfo : MainActivity.folderAppInfos){

            //如果应该在此文件夹中显示
            if(folderInfo.getWhere().equals(folderName)){
                AppInfo appInfo = new AppInfo();

                appInfo.setPackageName(folderInfo.getPackageName());    //获取包名
                appInfo.setAppName(folderInfo.getAppName());    //获取应用名
                appInfo.setCls(folderInfo.getCls());    //只对app有意义，获取主类
                appInfo.setAppIcon(folderInfo.getAppIcon());    //获取应用图标（后期要优化）
                appInfo.setFolder(false);    //必定不是文件夹，无意义
                appInfo.setWhere("");    //已在文件夹中，无意义

                appInfos.add(appInfo);
            }
        }

    }

    /**
     * ActivityInfo转Drawable
     * @param info
     * @return
     */
    private Drawable getResIconFormActyInfo(ActivityInfo info) {
        Resources resources;
        try {
            resources = packageManager.getResourcesForApplication(
                    info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }

        if (resources != null) {
            //如果获取applicationInfo结果不为空
            int iconId = info.getIconResource();
            if (iconId != 0) {
                //如果为空
                return getResIconFormActyInfo(resources, iconId);
            }
        }
        return getDefaultIcon();
    }


    private Drawable getResIconFormActyInfo(Resources resources, int iconId) {
        Drawable drawable;
        try {
            drawable = resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            drawable = null;
        }
        return (drawable != null) ? drawable : getDefaultIcon();
    }

    //获取一个默认的图标，避免为空的情况
    private Drawable getDefaultIcon() {
        return getResIconFormActyInfo(Resources.getSystem(),
                android.R.mipmap.sym_def_app_icon);
    }

    //外部获取信息的方法
    public ArrayList<AppInfo> getAppList() {
        loadAppsInfo();
        return appInfos;
    }

    public ArrayList<AppInfo> getFolderAppList(String folderName) {
        loadFolderInfo(folderName);
        return appInfos;
    }
}
