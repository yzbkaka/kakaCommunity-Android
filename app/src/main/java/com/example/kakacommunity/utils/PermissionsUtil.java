package com.example.kakacommunity.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsUtil {

    public static boolean showSystemSetting = true;

    private static final String TAG = "PermissionsUtil";

    private static volatile PermissionsUtil permissionsUtils = null;

    /**
     * 权限请求码
     */
    private final int mRequestCode = 100;

    /**
     * 回调接口
     */
    private IPermissionsResult mPermissionsResult;

    /**
     * 不再提示权限时的展示对话框
     */
    private AlertDialog mPermissionDialog;

    private PermissionsUtil() {
    }

    /**
     * 单例模式获取该封装对象
     *
     * @return PermissionsUtils单例对象
     */
    public static PermissionsUtil getInstance() {
        if (permissionsUtils == null) {
            synchronized (PermissionsUtil.class) {
                if (permissionsUtils == null) {
                    permissionsUtils = new PermissionsUtil();
                }
            }
        }
        return permissionsUtils;
    }

    /**
     * 检查权限
     *
     * @param context           Activity上下文
     * @param permissions       权限列表
     * @param permissionsResult 权限结果
     */
    public void checkPermissions(Activity context, String[] permissions, IPermissionsResult permissionsResult) {
        mPermissionsResult = permissionsResult;
        // 6.0才用动态权限
        if (Build.VERSION.SDK_INT < 23) {
            permissionsResult.passPermissions();
            Log.d(TAG, "sdk is less 23");
            return;
        }
        // 创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
        List<String> permissionList = new ArrayList<>();
        // 逐个判断需要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }
        // 有权限没有通过，需要申请
        if (permissionList.size() > 0) {
            Log.d(TAG, "need to get permission");
            ActivityCompat.requestPermissions(context, permissions, mRequestCode);
        } else {
            // 说明权限都已经通过
            permissionsResult.passPermissions();
            return;
        }
    }

    /**
     * 请求权限后回调的方法
     *
     * @param context      上下文Context
     * @param requestCode  自定义的权限请求码
     * @param permissions  请求的权限名称数组
     * @param grantResults 在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
     */
    public void onRequestPermissionResult(Activity context, int requestCode, String[] permissions, int[] grantResults) {
        // 有权限没有通过
        boolean hasPermissionDismiss = false;
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                    break;
                }
            }
            // 如果有权限没有被允许
            if (hasPermissionDismiss) {
                if (showSystemSetting) {
                    showSystemPermissionsSettingDialog(context);
                } else {
                    mPermissionsResult.forbidPermissions();
                }
            } else {
                // 全部权限通过，可以进行下一步操作
                mPermissionsResult.passPermissions();
            }
        }
    }

    /**
     * 展示系统权限的对话框
     *
     * @param context 上下文
     */
    private void showSystemPermissionsSettingDialog(final Activity context) {
        final String mPackName = context.getPackageName();
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(context)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            Uri packageURI = Uri.parse("package:" + mPackName);
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            context.startActivity(intent);
                            context.finish();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 关闭页面或者做其他操作
                            cancelPermissionDialog();
                            mPermissionsResult.forbidPermissions();
                        }
                    }).create();
        }
        mPermissionDialog.show();
    }

    /**
     * 关闭对话框
     */
    private void cancelPermissionDialog() {
        if (mPermissionDialog != null) {
            mPermissionDialog.cancel();
            mPermissionDialog = null;
        }
    }

    public interface IPermissionsResult {

        void passPermissions();

        void forbidPermissions();
    }
}
