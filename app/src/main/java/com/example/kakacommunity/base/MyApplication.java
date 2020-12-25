package com.example.kakacommunity.base;

import android.app.Application;
import android.content.Context;

import com.example.kakacommunity.db.MyDataBaseHelper;

public class MyApplication extends Application {

    private static Context context;

    private MyDataBaseHelper dataBaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        init();

    }

    private void init() {
        context = getApplicationContext();  //全局获取Context
        dataBaseHelper = MyDataBaseHelper.getInstance();  //初始化数据库
    }

    public static Context getContext() {
        return context;
    }
}
