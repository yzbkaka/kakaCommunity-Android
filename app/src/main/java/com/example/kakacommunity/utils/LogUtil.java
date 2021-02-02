package com.example.kakacommunity.utils;

import android.util.Log;

/**
 * 日志模块的封装，设置了日志开关
 */
public class LogUtil {

    /**
     * 是否开启日志，默认是开启
     */
    private static boolean debug = true;

    private LogUtil() {
    }

    public static void setDebug(boolean debug) {
        LogUtil.debug = debug;
    }

    public static boolean isDebug() {
        if (debug) {
            return true;
        } else {
            return false;
        }
    }

    public static void v(String tag, String msg) {
        if (debug) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (debug) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (debug) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (debug) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (debug) {
            Log.e(tag, msg);
        }
    }
}
