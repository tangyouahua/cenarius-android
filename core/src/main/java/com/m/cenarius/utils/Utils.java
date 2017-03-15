package com.m.cenarius.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.m.cenarius.R;

import java.util.List;

public class Utils {

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


//    public static String hash(String source) {
//        try {
//            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
//            byte[] md5Bytes = messageDigest.digest(source.getBytes("UTF-8"));
//            return ByteString.of(md5Bytes).hex();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return source;
//    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getAppVersionName() {
        PackageManager manager = AppContext.getInstance().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(AppContext.getInstance().getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean isRunningForeground(Activity activity) {
        android.app.ActivityManager activityManager = (android.app.ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        // 枚举进程
        if(appProcessInfos != null && appProcessInfos.size() > 0){
            for (android.app.ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
                if (appProcessInfo.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    if (appProcessInfo.processName.equals(activity.getApplicationInfo().processName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
