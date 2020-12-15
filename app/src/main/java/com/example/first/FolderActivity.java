package com.example.first;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.first.adapter.FolderAdapter;
import com.example.first.adapter.RvAdapter;
import com.example.first.base.AppInfo;
import com.example.first.utils.GetAppsInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static com.example.first.broadcast.MediaReceiver.filePath;
import static com.example.first.broadcast.MediaReceiver.getUsb;


public class FolderActivity extends Activity {
    private String folderName;    //文件夹标志

    private RelativeLayout rl_qDialog;
    private RecyclerView rv_icons;
    private RvAdapter appsAdapter;
    private PackageManager packageManager;

    private PopupWindow mPopWindow;

    //要在本文件夹中显示的文件
    private ArrayList<AppInfo> folderInfos = new ArrayList<AppInfo>();

    private AppInfo addBtn = new AppInfo();    //加在末尾，点击增减icon

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        rl_qDialog = findViewById(R.id.rl_qDialog);
        rv_icons = findViewById(R.id.rv_icons);

        Intent intent = getIntent();
        folderName = intent.getStringExtra("foldername");

        Resources resources = getResources();
        Drawable drawable = resources.getDrawable(R.drawable.add_icon);
        addBtn.setAppName("");
        addBtn.setAppIcon(drawable);
        addBtn.setWhere("addMore");    //该属性对文件夹中其它app已无意义，设它为标志不会冲突

        init();
        sysData();

        rl_qDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestroy();
            }
        });

    }

    /**
     * 初始化
     */
    private void init(){
        folderInfos = new GetAppsInfo(FolderActivity.this).getFolderAppList(folderName);
        folderInfos.add(addBtn);    //增加添加按钮
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rv_icons.setLayoutManager(gridLayoutManager);
        appsAdapter = new RvAdapter(folderInfos, packageManager);
        sysData();
        rv_icons.setAdapter(appsAdapter);
    }

    public void sysData(){
        appsAdapter.setOnItemClickListener(new RvAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                AppInfo info = folderInfos.get(position);

                if(info.getWhere().equals("addMore")){
                    //添加更多
                    showPopupWindow();
                }else{
                    //打开应用
                    String pkg = info.getPackageName();    //该应用的包名
                    String cls = info.getCls();    //应用的主activity类

                    ComponentName componet = new ComponentName(pkg, cls);

                    Intent i = new Intent();
                    i.setComponent(componet);
                    startActivity(i);
                }
            }
            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
    }
    /**
     * 在用户作出选择后调用，整理apps位置
     */
    private void reOrganize(){
        ArrayList<AppInfo> dToF = new ArrayList<>();
        ArrayList<AppInfo> FTod = new ArrayList<>();

        for(AppInfo dTofolder: MainActivity.appInfos){
            if(dTofolder.getWhere().equals(folderName)){
                //将其移入本文件夹
                AppInfo finfo = new AppInfo();
                finfo.setPackageName(dTofolder.getPackageName());
                finfo.setAppName(dTofolder.getAppName());
                finfo.setCls(dTofolder.getCls());
                finfo.setAppIcon(dTofolder.getAppIcon());
                finfo.setFolder(dTofolder.isFolder());
                finfo.setWhere(dTofolder.getWhere());

                MainActivity.folderAppInfos.add(finfo);
                dToF.add(dTofolder); //临时保存要删除的元素
            }
        }
        MainActivity.appInfos.removeAll(dToF);

        for(AppInfo fTodesk:MainActivity.folderAppInfos){
            if(fTodesk.getWhere().equals("desk")){
                //将其移至桌面
                AppInfo dinfo = new AppInfo();
                dinfo.setPackageName(fTodesk.getPackageName());
                dinfo.setAppName(fTodesk.getAppName());
                dinfo.setCls(fTodesk.getCls());
                dinfo.setAppIcon(fTodesk.getAppIcon());
                dinfo.setFolder(fTodesk.isFolder());
                dinfo.setWhere(fTodesk.getWhere());

                MainActivity.appInfos.add(dinfo);
                FTod.add(fTodesk);
            }
        }
        MainActivity.folderAppInfos.removeAll(FTod);
    }

    /**
     * 显示选择弹窗
     */
    private void showPopupWindow() {
        //设置contentView
        View contentView = LayoutInflater.from(FolderActivity.this).inflate(R.layout.popwin, null);
        mPopWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);

        //设置各个控件的点击响应
        final GridView gv_choose = contentView.findViewById(R.id.gv_choose);
        TextView tv_confirm = contentView.findViewById(R.id.tv_confirm);
        TextView tv_cancel = contentView.findViewById(R.id.tv_cancel);

        final ArrayList<AppInfo> infoChoose = new ArrayList<>();

        for(AppInfo info: folderInfos){
            //添加本文件夹中的app，供用户取消
            if(info.getWhere().equals("addMore"))
                break;

            AppInfo infoAdd = new AppInfo();
            infoAdd.setPackageName(info.getPackageName());
            infoAdd.setAppName(info.getAppName());
            infoAdd.setCls(info.getCls());
            infoAdd.setAppIcon(info.getAppIcon());
            infoAdd.setFolder(info.isFolder());
            infoAdd.setWhere(info.getWhere());

            infoChoose.add(infoAdd);
        }
        for(AppInfo info:MainActivity.appInfos){
            //添加桌面app，供用户选择
            if(!info.isFolder()) {
                AppInfo infoAdd = new AppInfo();
                infoAdd.setPackageName(info.getPackageName());
                infoAdd.setAppName(info.getAppName());
                infoAdd.setCls(info.getCls());
                infoAdd.setAppIcon(info.getAppIcon());
                infoAdd.setFolder(info.isFolder());
                infoAdd.setWhere(info.getWhere());

                infoChoose.add(infoAdd);
            }
        }

        final FolderAdapter folderAdapter = new FolderAdapter(FolderActivity.this, infoChoose);
        final ArrayList<String> isIconChecked = new ArrayList<>();

        for(AppInfo appInfo: infoChoose){    //初始化
            if(isIconChecked.size() < folderInfos.size()-1)
                isIconChecked.add("here");
            else
                isIconChecked.add("desk");
        }

        gv_choose.setAdapter(folderAdapter);
        gv_choose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //解决getChildAt位置不对的问题（index从可见部分算起）
                CheckBox chb = gv_choose.getChildAt(position - gv_choose.getFirstVisiblePosition()).findViewById(R.id.cb);

                //输出被点击Item的position（无误）
                Log.d("position","This is"+position);

                if(isIconChecked.get(position).equals("here")){    //此时chb处于被选中状态
                    chb.setChecked(false);    //取消选中
                    isIconChecked.set(position, "desk");    //标志：预备移到桌面
                }else{
                    chb.setChecked(true);
                    isIconChecked.set(position, "here");
                }
                //提示读取成功

            }
        });

        //取消本次修改
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopWindow.dismiss();
            }
        });

        //确定修改的内容
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检查用户选择结果

                for(AppInfo info: infoChoose){
                    int index = infoChoose.indexOf(info);  //获取元素下标

                    if(index + 1 > folderInfos.size()-1){    //size()-1排除加号按钮
                        //如果本来是桌面上的
                        if (isIconChecked.get(index).equals("here")){
                            //如果要移入这里
                            //将桌面的应用移入这里，修改where，回到MainActivity时统一挪数组
                            for(AppInfo destIn: MainActivity.appInfos){
                                //找到对应的app
                                if(destIn.getPackageName().equals(info.getPackageName()) && destIn.getAppName().equals(info.getAppName())){
                                    destIn.setWhere(folderName);
                                }
                            }
                        }
                    }else{
                        //如果本来是文件夹里的
                        if(isIconChecked.get(index).equals("desk")){
                            //要移除至桌面
                            for(AppInfo destOut: MainActivity.folderAppInfos){
                                //找到对应的app
                                if(destOut.getPackageName().equals(info.getPackageName()) && destOut.getAppName().equals(info.getAppName())){
                                    destOut.setWhere("desk");
                                }
                            }
                        }
                    }
                }

                reOrganize();

                mPopWindow.dismiss();

                init();
            }
        });

        //显示PopupWindow
        View rootview = LayoutInflater.from(FolderActivity.this).inflate(R.layout.activity_main, null);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopWindow.setFocusable(true);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        finish();
    }

    @Override
    protected  void  onPause()
    {
        super .onPause();
        finish();
    }

    @Override
    public  void  finish()
    {
        //当前Activity销毁时，主Activity更新
        Intent intent = new Intent();
        setResult(1001,intent);
        super .finish();
    }

}
