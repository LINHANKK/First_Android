package com.example.first.base;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BaseApplication extends Application {
    public static int WRITE_EXTERNAL_STORAGE=1;
    public static int CODE_FOR_CAMERA_PERMISSION=2;

}
