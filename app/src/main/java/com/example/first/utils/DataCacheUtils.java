package com.example.first.utils;

import android.content.Context;
import android.util.Log;

import com.example.first.base.AppInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * @Author LinHan
 * @Create 2020/12/23 16:09
 * @Description
 **/

public class DataCacheUtils {

    /**
     * 定义一些你项目里面的 缓存文件名字 ，自定义，不要也没关系，调用函数再传入也行
     */


    private static String DataCache = "Data_Cache_File";

    /**
     * 保存 一组 数据
     *
     * @param ctx       上下文
     * @param data      种子
     * @param cacheName 缓存文件名
     */
    public static void saveListCache(Context ctx, ArrayList<AppInfo> data, String cacheName) {
        new DataCache<AppInfo>().saveGlobal(ctx, data, cacheName);
    }

    /**
     * 直接根据 缓存文件名获取
     */
    public static ArrayList<AppInfo> loadListCache(Context ctx, String cacheName) {
        return new DataCache<AppInfo>().loadGlobal(ctx, cacheName);
    }

    /**
     *
     * @param <AppInfo> 数据缓存 save or load
     */
    static class DataCache<AppInfo> {
        public void save(Context ctx, ArrayList<AppInfo> data, String name) {
            save(ctx, data, name, "");
        }

        public void saveGlobal(Context ctx, ArrayList<AppInfo> data, String name) {
            save(ctx, data, name, DataCache);
        }

        private void save(Context ctx, ArrayList<AppInfo> data, String name, String folder) {
            if (ctx == null) {
                return;
            }
            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }
            if (file.exists()) {
                file.delete();
            }
            Log.d("everb", file.getAbsolutePath());
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(data);
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<AppInfo> load(Context ctx, String name) {
            return load(ctx, name, "");
        }

        public ArrayList<AppInfo> loadGlobal(Context ctx, String name) {
            return load(ctx, name, DataCache);
        }

        private ArrayList<AppInfo> load(Context ctx, String name, String folder) {
            ArrayList<AppInfo> data = null;

            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }
            Log.d("everb", "file " + file.getAbsolutePath());
            if (file.exists()) {
                try {
                    Log.d("everb", "write object");
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    data = (ArrayList<AppInfo>) ois.readObject();
                    ois.close();
                } catch (Exception e) {
                    Log.d("everb", e.toString());
                }
            }
            if (data == null) {     /** 如果没有 */
                Log.d("everb", "data == null");
                data = new ArrayList<AppInfo>();
            }
            return data;
        }
    }

}