package com.example.first.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.first.base.AppInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;


public class SPUtil {

        private SharedPreferences preferences;
        private SharedPreferences.Editor editor;
        private Context context;
        private List<AppInfo> appInfo=new ArrayList<>();

        public SPUtil(Context context, String fileName) {
            preferences = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
            editor = preferences.edit();
            this.context=context;
        }
    public void setSPInfo(List<AppInfo> namelist){
        SharedPreferences sp = context.getSharedPreferences("SP_NewUserModel_List", Activity.MODE_PRIVATE);//创建sp对象
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String jsonStr = gson.toJson(namelist); //将List转换成Json
        SharedPreferences.Editor editor = sp.edit() ;
        editor.remove("KEY_NewUserModel_LIST_DATA");
        editor.commit() ;
        editor.putString("KEY_NewUserModel_LIST_DATA", jsonStr) ; //存入json串
        editor.commit() ;  //提交
    }
    public List<AppInfo> getSPInfo(){
        SharedPreferences sp = context.getSharedPreferences("SP_NewUserModel_List",Activity.MODE_PRIVATE);//创建sp对象,如果有key为"SP_PEOPLE"的sp就取出
        String peopleListJson = sp.getString("KEY_NewUserModel_LIST_DATA","");  //取出key为"KEY_PEOPLE_DATA"的值，如果值为空，则将第二个参数作为默认值赋值
        if(peopleListJson!="")  //防空判断
        {
            Gson gson = new Gson();
            appInfo = gson.fromJson(peopleListJson, new TypeToken<List<AppInfo>>() {}.getType()); //将json字符串转换成List集合
            return appInfo;
        }else {
            return  null;
        }
    }

    }

