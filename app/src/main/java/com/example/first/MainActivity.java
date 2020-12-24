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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.first.adapter.RvAdapter;
import com.example.first.base.AppInfo;
import com.example.first.base.BaseApplication;
import com.example.first.broadcast.PackageChangeReceiver;
import com.example.first.broadcast.PackageDeleteReceiver;
import com.example.first.utils.DataCacheUtils;
import com.example.first.utils.GetAppsInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
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
        LitePal.getDatabase();
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
        List<AppInfo> allApps = DataSupport.findAll(AppInfo.class);
        if (allApps.size() != 0) {
            updateImg(allApps);
            Log.e("appcount111",allApps.size()+"");
            appInfos = (ArrayList<AppInfo>) allApps;
            //查看app图标
            Log.e("appIcon", String.valueOf(allApps.get(0).getAppIcon()));
            Log.e("appName", allApps.get(0).getAppName());
        }else {
            appInfos = loadAppsInfo();
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(gridLayoutManager);
        rvAdapter = new RvAdapter(appInfos,packageManager);
        recyclerView.setAdapter(rvAdapter);
        Log.e("appcount",appInfos.size()+"");
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

            //长按应用：卸载功能，更换图标，新建文件夹
            @Override
            public void onItemLongClick(View view, int position) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_item, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        ArrayList<AppInfo> apps1 = new ArrayList<>(appInfos);
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
                                    //更换app图标
                                    if (getUsb) {   //判断是否插入U盘
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
                                    appInfos = apps1;
                                    rvAdapter.setResolveInfo(appInfos);
                                    break;

                                case R.id.increaseApps:
                                    //新增文件夹
                                    AppInfo info1 = apps1.get(position);
                                    if(info1.isFolder()) {    //如果已经是文件夹
                                        break;
                                    }

                                    //使用自定义View创建新文件夹图标，替代原本的app图标
                                    //自定义View加入GridView，刷新页面
                                    Resources resources = getResources();
                                    Drawable drawable = resources.getDrawable(R.drawable.folder);

                                    //保留：包名、app名、类名、图标、是否为文件夹
                                    AppInfo infoInFolder = new AppInfo(info1.getAppName(),info1.getPackageName(),info1.getCls(),
                                            info1.getAppIcon(),info1.isFolder(),"");

                                    //将当前position的app信息修改为文件夹信息
                                    info1.setAppIcon(drawable);
                                    info1.setFolder(true);    //提示本应用为文件夹
                                    countFolder++;    //文件夹id递增
                                    info1.setPackageName(String.valueOf(countFolder));
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
    protected void onResume() {
//        if (getSPInfo(this, "allApps") != null){
//            List<AppInfo> apps2 = new ArrayList<>(appInfos);
//            apps2 = getSPInfo(this, "allApps");
//            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
//            recyclerView.setLayoutManager(gridLayoutManager);
//            rvAdapter = new RvAdapter(apps2,packageManager);
//            recyclerView.setAdapter(rvAdapter);
//            rvAdapter.setResolveInfo(apps2);
//            Log.e("appssssss", ""+getSPInfo(this, "allApps").size());
//            Log.e("appsssssssss",apps2.get(0).toString());
//        }
//        if (DataCacheUtils.loadListCache(this, "allApps") != null){
//            Log.e("aaaaaaaaaaaaaaaaaaa", String.valueOf(DataCacheUtils.loadListCache(this, "allApps").size()));
//        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        setSPInfo(appInfos, this, "allApps");
//        DataCacheUtils.saveListCache(this, appInfos, "allApps");
//        for(AppInfo allApp : appInfos) {
//            allApp.setImgStr(drawableToByte(allApp.getAppIcon()));
//            allApp.save();
//        }
        updateImgStr();
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

    public synchronized Drawable byteToDrawable(String icon) {
        byte[] img= Base64.decode(icon.getBytes(), Base64.DEFAULT);
        Bitmap bitmap;
        if (img != null) {
            bitmap = BitmapFactory.decodeByteArray(img,0, img.length);
            @SuppressWarnings("deprecation")
            Drawable drawable = new BitmapDrawable(bitmap);

            return drawable;
        }
        return null;
    }

    public  synchronized  String drawableToByte(Drawable drawable) {
        if (drawable != null) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            int size = bitmap.getWidth() * bitmap.getHeight() * 4;

            // 创建一个字节数组输出流,流的大小为size
            ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
            // 设置位图的压缩格式，质量为100%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            // 将字节数组输出流转化为字节数组byte[]
            byte[] imagedata = baos.toByteArray();

            String icon= Base64.encodeToString(imagedata, Base64.DEFAULT);
            return icon;
        }
        return null;
    }

    private void updateImgStr() {
        new Thread(){
            @Override
            public void run() {
                for(int i = 0; i < appInfos.size(); i++) {
                    appInfos.get(i).setImgStr(drawableToByte(appInfos.get(i).getAppIcon()));
                    appInfos.get(i).save();
                }
//                app.setImgStr(drawableToByte(app.getAppIcon()));
//                app.save();
//                Log.e("aapppsize", appInfos.size()+"");
            }
        }.start();
    }

    private List<AppInfo> updateImg(List<AppInfo> list) {
        List<AppInfo> list1 = new ArrayList<>();
        new Thread(){
            @Override
            public void run() {
                for(int i = 0; i < list.size(); i++) {
                    list.get(i).setAppIcon(byteToDrawable(list.get(i).getImgStr()));
                    list1.add(list.get(i));
                }
            }
        }.start();
        return list1;
    }

//    public void setSPInfo(List<AppInfo> namelist, Context context, String name){
//        SharedPreferences sp = context.getSharedPreferences(name, Activity.MODE_PRIVATE);//创建sp对象
//        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
//        String jsonStr = gson.toJson(namelist); //将List转换成Json
//        SharedPreferences.Editor editor = sp.edit() ;
//        editor.remove("KEY_NewUserModel_LIST_DATA");
//        editor.apply();
//        editor.putString("KEY_NewUserModel_LIST_DATA", jsonStr) ; //存入json串
//        editor.commit() ;  //提交
//    }
//
//    public List<AppInfo> getSPInfo(Context context, String name){
//        SharedPreferences sp = context.getSharedPreferences(name,Activity.MODE_PRIVATE);//创建sp对象,如果有key为"SP_PEOPLE"的sp就取出
//        String peopleListJson = sp.getString("KEY_NewUserModel_LIST_DATA","");  //取出key为"KEY_PEOPLE_DATA"的值，如果值为空，则将第二个参数作为默认值赋值
//        if(!peopleListJson.equals(""))  //防空判断
//        {
//            List<AppInfo> list11 = new ArrayList<AppInfo>();
//            Gson gson = new Gson();
//            list11 = gson.fromJson(peopleListJson, new TypeToken<List<AppInfo>>() {}.getType()); //将json字符串转换成List集合
//            return list11;
//        }else {
//            return null;
//        }
//    }
//
}