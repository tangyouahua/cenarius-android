package com.m.cenarius;

import android.content.Context;
import android.text.TextUtils;

import com.m.cenarius.resourceproxy.ResourceProxy;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.AppContext;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class Cenarius {

    public static final String TAG = Cenarius.class.getSimpleName();

    public static boolean DEBUG = false;
    public static boolean DevelopModeEnable = false;
    public static String LoginService;
    public static String LoginAppKey;
    public static String LoginAppSecret;
    public static ArrayList<String> routesWhiteList;

    /**
     * 可以额外设置主app的user-agent
     */
    private static String mHostUserAgent;

    /**
     * 可以通过设置OkHttpClient的方式实现共用
     */
    private static OkHttpClient mOkHttpClient;

    public static void initialize(final Context context) {
        AppContext.init(context);
        RouteManager.getInstance();
        ResourceProxy.getInstance();
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    /**
     * 设置开发模式
     */
    public static void setDevelopModeEnable(boolean enable){
        DevelopModeEnable = enable;
    }

    public static void setLogin(String service, String appKey, String appSecret){
        LoginService = service;
        LoginAppKey = appKey;
        LoginAppSecret = appSecret;
    }

    /**
     * 设置额外的User-Agent信息，在发起请求的时候会带上
     *
     * @param hostUserAgent
     */
    public static void setHostUserAgent(String hostUserAgent) {
        mHostUserAgent = hostUserAgent;
    }

    /**
     * 获取User-Agent信息，在发起请求的时候会带上
     *
     * @return
     */
    public static String getUserAgent() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Cenarius-Core/").append(BuildConfig.VERSION_NAME);
        if (!TextUtils.isEmpty(mHostUserAgent)) {
            stringBuilder.append(" ").append(mHostUserAgent);
        }
        return stringBuilder.toString();
    }

    public static void setOkHttpClient(OkHttpClient okHttpClient) {
        if (null != okHttpClient) {
            mOkHttpClient = okHttpClient;
        }
    }

    public static OkHttpClient getOkHttpClient() {
        if (null == mOkHttpClient) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
                    .build();
        }
        return mOkHttpClient;
    }

    /**
     设置路由表白名单
     */
    public static void setRoutesWhiteList(ArrayList<String> whiteList){
        routesWhiteList = whiteList;
    }

}
