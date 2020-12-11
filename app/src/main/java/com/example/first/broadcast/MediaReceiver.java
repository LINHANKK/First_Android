package com.example.first.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;

/**
 * 广播
 * 获取USB挂载路径
 */
public class MediaReceiver extends BroadcastReceiver {
    public static boolean getUsb = false;
    public static String filePath;

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_MEDIA_CHECKING:
                break;
            case Intent.ACTION_MEDIA_MOUNTED:
                // 获取挂载路径, 读取U盘文件
                Uri uri = intent.getData();
                if (uri != null) {
                    filePath = uri.getPath();
                    getUsb = true;
                    File rootFile = new File(filePath);
                    //for (File file : rootFile.listFiles()) {
                        // 文件列表...
                    //}

                    //提示读取成功
                    Toast.makeText(context, "USB read "+filePath, Toast.LENGTH_SHORT).show();
                }
                break;
            case Intent.ACTION_MEDIA_EJECT:
                getUsb = false;
                //提示读取成功
                Toast.makeText(context, "USB eject", Toast.LENGTH_SHORT).show();
                break;
            case Intent.ACTION_MEDIA_UNMOUNTED:
                break;
        }

    }
}
