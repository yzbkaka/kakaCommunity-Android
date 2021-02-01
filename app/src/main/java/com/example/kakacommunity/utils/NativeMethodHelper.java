package com.example.kakacommunity.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NativeMethodHelper {

    private static volatile NativeMethodHelper nativeMethodHelper = null;

    private NativeMethodHelper() {
    }

    public static NativeMethodHelper getInstance() {
        if (nativeMethodHelper == null) {
            synchronized (NativeMethodHelper.class) {
                if (nativeMethodHelper == null) {
                    nativeMethodHelper = new NativeMethodHelper();
                }
            }
        }
        return nativeMethodHelper;
    }

    /**
     * 加载library
     */
    public void init() {
        System.loadLibrary("native-lib");
    }

    /**
     * 获得全局环境
     */
    public native void getEnv();

    /**
     * 开始进行hook操作
     */
    public native void startHook();

    /**
     * 停止hook操作
     */
    public native void stopHook();

    /**
     * 打出堆栈
     */
    public static synchronized void printStackTrace() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Exception exception = new Exception();
        exception.printStackTrace(new PrintStream(stream, true));
        Log.d("ndk", new String(stream.toByteArray()));
        Log.d("ndk", "------------------------------");
    }
}
