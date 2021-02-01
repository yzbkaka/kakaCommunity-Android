package com.example.kakacommunity.base;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.kakacommunity.db.MyDataBaseHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    static {
        System.loadLibrary("native-lib");  //加载NDK
    }


    public native String StringFromJNI();
}
