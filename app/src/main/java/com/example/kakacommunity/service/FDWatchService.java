package com.example.kakacommunity.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FDWatchService extends Service {

    /**
     * 开始读取的下标
     */
    public static final int START_INDEX = 8;

    /**
     * 结束读取的下标
     */
    public static final int END_INDEX = 26;

    private static final String TAG = "FDWatchService";

    /**
     * fd数量最大限制
     */
    private int mMaxOpenFiles;

    public FDWatchService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"CREATE");
        mMaxOpenFiles = getMaxOpenFiles();
        Log.e(TAG, "start FDWatchService:" + "current pid is " + getPid() + " ,max open files is " + mMaxOpenFiles);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    File fdFile = new File("/proc/" + getPid() + "/fd");
                    File[] files = fdFile.listFiles();
                    int length = files.length;
                    if (length >= mMaxOpenFiles * 0.9) {
                        Log.e(TAG, "FD number reached early warning");
                        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
                        Set<Thread> threads = allStackTraces.keySet();
                        for (Thread thread : threads) {
                            StringBuffer stackTraceMessage = new StringBuffer();
                            StackTraceElement[] trace = thread.getStackTrace();
                            for (int i = 0; i < trace.length; i++) {
                                // 统计所有堆栈信息，进行数据收集和分析（上报）
                                stackTraceMessage.append(trace[i]);
                            }
                            Log.e(TAG,stackTraceMessage.toString());
                        }
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 获得当前进程的pid
     *
     * @return 进程tid
     */
    private int getPid() {
        int pid = android.os.Process.myPid();
        return pid;
    }

    /**
     * 获得当前设备fd数量的最大限制值
     *
     * @return fd数量的最大值
     */
    private int getMaxOpenFiles() {
        Process process = null;
        BufferedReader input = null;
        List<String> processList = new ArrayList<String>();
        try {
            process = Runtime.getRuntime().exec("cat /proc/" + getPid() + "/limits");
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = input.readLine()) != null) {
                processList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String line = processList.get(START_INDEX);
        int i = END_INDEX;
        for (i = END_INDEX; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                break;
            }
        }
        String numString = line.substring(END_INDEX, i);
        if (isNumeric(numString)) {
            return Integer.parseInt(numString);
        }
        Log.e(TAG, "failed to get max open files");
        return 0;
    }

    /**
     * 判断字符串是否是数字
     *
     * @param str 传入的字符串
     * @return 如果是数字返回true，否则返回false
     */
    public static boolean isNumeric(String str) {
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            // 异常 说明包含非数字。
            return false;
        }
        return true;
    }
}

