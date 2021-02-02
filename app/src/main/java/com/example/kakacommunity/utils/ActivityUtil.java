package com.example.kakacommunity.utils;

import android.app.Activity;
import android.os.Build;

/**
 * Activity工具类
 */
public class ActivityUtil {

    /**
     * 判断Activity是否可见
     */
    public static boolean isDestroy(Activity activity) {
        return activity == null || activity.isFinishing() ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed());
    }
}
