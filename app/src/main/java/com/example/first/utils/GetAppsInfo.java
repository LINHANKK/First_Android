package com.example.first.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.example.first.base.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class GetAppsInfo {

    private PackageManager packageManager;
    private int mIconDpi;
    private List<AppInfo> appInfos = new ArrayList<AppInfo>();

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
            appInfo.setPackageName(resolveInfo.activityInfo.applicationInfo.packageName);
            appInfo.setAppName(resolveInfo.loadLabel(packageManager).toString());
            appInfo.setAppIcon(getResIconFormActyInfo(resolveInfo.activityInfo));
            appInfo.setCls(resolveInfo.activityInfo.name);
            appInfos.add(appInfo);
        }
    }

    private Drawable getResIconFormActyInfo(ActivityInfo info) {
        Resources resources;
        try {
            resources = packageManager.getResourcesForApplication(
                    info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.getIconResource();
            if (iconId != 0) {
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
    public List<AppInfo> getAppList() {
        loadAppsInfo();
        return appInfos;
    }
}
