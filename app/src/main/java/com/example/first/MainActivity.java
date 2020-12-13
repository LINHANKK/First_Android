package com.example.first;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import com.example.first.broadcast.PackageDeleteReceiver;
import com.example.first.utils.GetAppsInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.example.first.broadcast.MediaReceiver.filePath;
import static com.example.first.broadcast.MediaReceiver.getUsb;

public class MainActivity extends AppCompatActivity {
    public static ArrayList<AppInfo> appInfos;    //显示在桌面的（包括app和文件夹）
    public static ArrayList<AppInfo> folderAppInfos;    //在文件夹中的app
    private int countFolder;    //类比自增id，唯一标志每个文件夹。初始为0

    private RecyclerView recyclerView;
    private PackageManager packageManager;
    private ConstraintLayout linearLayout;
    private RvAdapter rvAdapter;

    PackageChangeReceiver mReceiver = new PackageChangeReceiver();
    PackageDeleteReceiver delReceiver = new PackageDeleteReceiver();
    IntentFilter filter = new IntentFilter();
    IntentFilter filter_del = new IntentFilter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        countFolder = 0;
        folderAppInfos = new ArrayList<AppInfo>();
        initView();
        initData();
        onLauncher();
        check();
    }

    private void initData() {
        filter.addAction("android.intent.action.PACKAGE_ADDED");    //安装应用
        filter_del.addAction("android.intent.action.PACKAGE_REMOVED");    //卸载应用
        filter.addDataScheme("package");    //隐式意图 需匹配Data
        filter_del.addDataScheme("package");    //隐式意图 需匹配Data
    }

    private void initView() {
        recyclerView = findViewById(R.id.rv);
        linearLayout = findViewById(R.id.linearLayout);
    }


    public ArrayList<AppInfo> loadAppsInfo() {
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
                if (info.isFolder()){
                    Intent intent = new Intent();
                    intent.putExtra("foldername", info.getPackageName());
                    intent.setClass(MainActivity.this, FolderActivity.class);
                    startActivityForResult(intent,1000);
                }else {
                    String pkg = info.getPackageName();    //该应用的包名
                    String cls = info.getCls();    //应用的主activity类
                    ComponentName componet = new ComponentName(pkg, cls);
                    Intent i = new Intent();
                    i.setComponent(componet);
                    startActivity(i);
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
                                    info.setWhere("del");
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

                                //新增文件夹
                                case R.id.increaseApps:
                                    AppInfo info1 = apps1.get(position);
                                    if(info1.isFolder()) {    //如果已经是文件夹
                                        break;
                                    }

                                    //使用自定义View创建新文件夹图标，替代原本的app图标
                                    //自定义View加入GridView，刷新页面
                                    Resources resources = getResources();
                                    Drawable drawable = resources.getDrawable(R.drawable.ic_folder);

                                    AppInfo infoInFolder = new AppInfo();
                                    //保留：包名、app名、类名、图标、是否为文件夹
                                    infoInFolder.setPackageName(info1.getPackageName());
                                    infoInFolder.setAppName(info1.getAppName());
                                    infoInFolder.setCls(info1.getCls());
                                    infoInFolder.setAppIcon(info1.getAppIcon());
                                    infoInFolder.setFolder(info1.isFolder());

                                    //将当前position的app信息修改为文件夹信息
                                    info1.setAppIcon(drawable);
                                    info1.setFolder(true);    //提示本应用为文件夹
                                    countFolder++;    //文件夹id递增
                                    info1.setPackageName(String.valueOf( countFolder));
                                    info1.setAppName("文件夹");

                                    //表示该app放在文件夹中
                                    infoInFolder.setWhere(info1.getPackageName());    //显示位置设为 该文件夹的唯一标志

                                    folderAppInfos.add(infoInFolder);

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

        delReceiver.OnPackageDeleteListener(new PackageDeleteReceiver.PackageDeleteListener() {
            @Override
            public void packageDelete() {
                int index = 0;
                for(AppInfo delInfo: appInfos){
                    if(delInfo.getWhere().equals("del")){
                        //找到需要删除的图标
                        index = appInfos.indexOf(delInfo);
                        break;
                    }
                }
                appInfos.remove(index);
                rvAdapter.setResolveInfo(appInfos);
            }
        });
        registerReceiver(delReceiver, filter_del);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(delReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == 1000 && resultCode == 1001){
            ArrayList<AppInfo> list1 = new ArrayList<>(appInfos);
            rvAdapter.setResolveInfo(list1);
        }
    }
}