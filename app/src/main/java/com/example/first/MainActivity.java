package com.example.first;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.first.adapter.RvAdapter;
import com.example.first.base.AppInfo;
import com.example.first.base.BaseApplication;
import com.example.first.broadcast.PackageChangeReceiver;
import com.example.first.utils.GetAppsInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.example.first.broadcast.MediaReceiver.filePath;
import static com.example.first.broadcast.MediaReceiver.getUsb;

public class MainActivity extends AppCompatActivity {

    private int UNINSTALL = 1;

    private RecyclerView recyclerView;
    private PackageManager packageManager;
    private List<AppInfo> appInfos;
    private ConstraintLayout linearLayout;
    private RvAdapter rvAdapter;
    private Context mContext;

    PackageChangeReceiver mReceiver = new PackageChangeReceiver();
    IntentFilter filter = new IntentFilter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        onLauncher();
        check();
        //changeWallPaper();
        //onSetWallpaper();
    }

    private void initData() {
        filter.addAction("android.intent.action.PACKAGE_ADDED");    //安装应用
        filter.addAction("android.intent.action.PACKAGE_REMOVED");    //卸载应用
        filter.addDataScheme("package");    //隐式意图 需匹配Data
    }

    private void initView() {
        recyclerView = findViewById(R.id.rv);
        linearLayout = findViewById(R.id.linearLayout);
    }


    public List<AppInfo> loadAppsInfo() {
        return new GetAppsInfo(MainActivity.this).getAppList();
    }

    public void onLauncher() {
        appInfos = loadAppsInfo();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        rvAdapter = new RvAdapter(appInfos,packageManager);
        recyclerView.setAdapter(rvAdapter);
        rvAdapter.setOnItemClickListener(new RvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                AppInfo info = appInfos.get(position);
                if (info.getPackageName() != null && info.getCls() != null){
                    ComponentName componentName = new ComponentName(info.getPackageName(), info.getCls());
                    Intent intent=new Intent();
                    intent.setComponent(componentName);
                    startActivity(intent);
                }else {
                    Intent intent1 = new Intent();
                    intent1.setClass(MainActivity.this, FolderActivity.class);
                    startActivity(intent1);
                }
            }

            //卸载应用
            @Override
            public void onItemLongClick(View view, int position) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_item, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        List<AppInfo> apps1 = new ArrayList<>(appInfos);
                            switch (item.getItemId()) {
                                case R.id.removeItem:
                                    //实现app删除功能
                                    Intent i = new Intent();
                                    AppInfo info = appInfos.get(position);
                                    //该应用的包名
                                    String pkg = info.getPackageName();
                                    Uri uri = Uri.parse("package:" + pkg);//获取待删除包名的URI
                                    i.setAction(Intent.ACTION_DELETE);//设置我们要执行的卸载动作
                                    i.setData(uri);//设置获取到的URI
                                    startActivityForResult(i, 0);
                                    break;

                                case R.id.changeIcon:
                                    if (getUsb) {
                                        Bitmap bitmap;
                                        try {
                                            bitmap = BitmapFactory.decodeStream(new FileInputStream(filePath + "/icon.png"));
                                        } catch (FileNotFoundException e) {
                                            bitmap = BitmapFactory.decodeStream(getClass().getResourceAsStream(""));
                                        }

                                        BitmapDrawable bd = new BitmapDrawable(bitmap);
                                        apps1.get(position).setAppIcon(bd);

                                    } else {
                                        Drawable drawable = getResources().getDrawable(R.drawable.icon);
                                        apps1.get(position).setAppIcon(drawable);
                                    }

                                    rvAdapter.setResolveInfo(apps1);

                                    break;

                                case R.id.increaseApps:
                                    AppInfo folder = new AppInfo("App文件夹", null
                                            , getResources().getDrawable(R.drawable.ic_folder), null);
                                    apps1.add(folder);
                                    rvAdapter.setResolveInfo(apps1);
                                    break;

                                default:
                                    break;
                            }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        mReceiver.OnPackageChangeListener(new PackageChangeReceiver.PackageChangeListener() {
            @Override
            public void packageChange() {
                appInfos = loadAppsInfo();
                rvAdapter.setResolveInfo(appInfos);
            }
        });
        registerReceiver(mReceiver,filter);
    }


    public void saveAppInfo(List<AppInfo> appList) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, FolderActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("appList", (Serializable) appList);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 动态权限申请
     */
    public void check(){
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(this.getApplication(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            //拥有权限，执行操作
            //changeWallPaper();
        }else{
            //没有权限，向用户请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}
                    , BaseApplication.WRITE_EXTERNAL_STORAGE);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //通过requestCode来识别是否同一个请求
        if (requestCode ==BaseApplication.WRITE_EXTERNAL_STORAGE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //用户同意，执行操作
                //changeWallPaper();
            }else{
                //用户不同意，向用户展示该权限作用
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setMessage("权限不允许")
                            .setPositiveButton("OK", (dialog1, which) ->
                                    ActivityCompat.requestPermissions(this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            BaseApplication.WRITE_EXTERNAL_STORAGE))
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }
            }
        }
    }

    public void onSetWallpaper() {
        //生成一个设置壁纸的请求
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,"chooser_wallpaper");
        //发送设置壁纸的请求
        startActivity(chooser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {

                    rvAdapter.setResolveInfo(loadAppsInfo());
                } else {

                    rvAdapter.setResolveInfo(loadAppsInfo());
                }
        }

    }
}