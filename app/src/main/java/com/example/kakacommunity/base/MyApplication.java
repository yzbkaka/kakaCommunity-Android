package com.example.kakacommunity.base;

import android.app.Application;
import android.content.Context;
import com.example.kakacommunity.db.MyDataBaseHelper;


/**
 * 全局Application类，冷启动
 */
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
        //初始化数据库
        dataBaseHelper = MyDataBaseHelper.getInstance();
        //启动监控
        /*Intent intent = new Intent(this, FDWatchService.class);
        startService(intent);*/

    }

    public static Context getContext() {
        return context;
    }

}
