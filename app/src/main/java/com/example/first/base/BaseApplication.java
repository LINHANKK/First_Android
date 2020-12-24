package com.example.first.base;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

public class BaseApplication extends LitePalApplication {
    public static int WRITE_EXTERNAL_STORAGE=1;
    public static String PICTURE_BYTE = "image";

}
