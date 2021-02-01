package com.example.kakacommunity.base;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.kakacommunity.db.MyDataBaseHelper;
import com.example.kakacommunity.service.FDWatchService;
import com.example.kakacommunity.utils.NativeMethodHelper;
import com.example.kakacommunity.utils.PermissionsUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MyApplication extends Application {

    private static Context context;

    private MyDataBaseHelper dataBaseHelper;

    private NativeMethodHelper nativeMethodHelper;


    @Override
    public void onCreate() {
        super.onCreate();
        init();

    }

    private void init() {
        context = getApplicationContext();  //全局获取Context

        //初始化数据库
        dataBaseHelper = MyDataBaseHelper.getInstance();

        //初始化ndk hook
        nativeMethodHelper = NativeMethodHelper.getInstance();
        //nativeMethodHelper.init();
        //nativeMethodHelper.getEnv();
        //nativeMethodHelper.startHook();

        //启动监控
        Intent intent = new Intent(this, FDWatchService.class);
        startService(intent);

    }

    public static Context getContext() {
        return context;
    }

    
}
