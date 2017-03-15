package com.m.cenarius.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.alibaba.fastjson.JSON;
import com.m.cenarius.Cenarius;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.utils.OpenApi;
import com.m.cenarius.view.CenariusWidget;

import org.apache.cordova.engine.SystemWebViewEngine;
import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.http.cookie.DbCookieStore;
import org.xutils.x;
import org.xwalk.core.XWalkCookieManager;

import java.net.HttpCookie;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class LoginWidget implements CenariusWidget {

    public static final String TAG = CenariusWidget.class.getSimpleName();

    private static final String PREF_FILE_NAME = "cenarius_login";
    private static final String TOKEN_KEY = "access_token";

    public static  boolean isX86;

    static {
        if(Build.CPU_ABI.toLowerCase().contains("x86") || Build.CPU_ABI2.toLowerCase().contains("x86") ){
            isX86 = true;
        }
    }

    public interface LoginCallback {
        void onSuccess(String accessToken);

        void onFail(String errorMessage);
    }

    @Override
    public String getPath() {
        return "/widget/login";
    }

    @Override
    public boolean handle(View view, String url) {
        HashMap dataMap = GsonHelper.getDataMap(url, getPath());
        if (dataMap != null) {
//            if (null != view && view.getContext() instanceof Activity) {
//                ((Activity) view.getContext()).setTitle((String) dataMap.get(KEY_TITLE));
//            }
            return true;
        }
        return false;
    }

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @param captchaId 获取验证码提交的随机码
     * @param captcha 验证码
     * @param callback  登录后将执行这个回调
     */
    public static void login(String username, String password, String captchaId, String captcha, final LoginCallback callback) {
        String service = Cenarius.LoginService;
        String appKey = Cenarius.LoginAppKey;
        String appSecret = Cenarius.LoginAppSecret;
        if (service == null || appKey == null || appSecret == null) {
            if (callback != null) {
                callback.onFail("先设置 service appKey appSecret");
            }
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("app_key", appKey);
        params.put("timestamp", Long.toString((new Date()).getTime()));
        params.put("username", username);
        params.put("password", password);
        params.put("terminalType", "MOBILE");
        params.put("rememberMe", "true");
        params.put("captchaId", captchaId);
        params.put("captcha", captcha);
        params.put("locale", "zh_CN");
        String sign = OpenApi.md5Signature(params, appSecret);
        params.put("sign", sign);
        LogUtils.i("登录参数：",params.toString());

        RequestParams requestParams = new RequestParams(service);
        for (String key : params.keySet()) {
            requestParams.addQueryStringParameter(key, String.valueOf(params.get(key)));
        }

        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                setWebViewCookies();
                Map map = JSON.parseObject(result, Map.class);
                LogUtils.i("登录结果：",map.toString());
                String token = (String) map.get("access_token");
                String error_msg = (String) map.get("error_msg");
                if (token != null && token.length() > 0) {
                    saveAccessToken(token);
                    if (callback != null) {
                        callback.onSuccess(token);
                    }
                } else {
                    if (callback != null) {
                        if (error_msg != null && error_msg.length() > 0) {
                            callback.onFail(error_msg);
                        } else {
                            callback.onFail("系统错误");
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtils.i("登录结果：",ex.toString());
                if (callback != null) {
                    callback.onFail("系统错误");
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 同步cookies到webview
     */
    public static void setWebViewCookies() {
        DbCookieStore instance = DbCookieStore.INSTANCE;
        List<HttpCookie> cookiesAll = instance.getCookies();
        // TODO Auto-generated method stub
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(AppContext.getInstance());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        //以下注释了防上出现cookie更新不同步问题
//		cookieManager.removeSessionCookie();
        String domainName="";
        XWalkCookieManager xwalkCookieManager =  null;
        if(!isX86){
            xwalkCookieManager =  new XWalkCookieManager();
            xwalkCookieManager.setAcceptCookie(true);
            xwalkCookieManager.removeAllCookie();
        }
        for(int i=0;i<cookiesAll.size();i++){
            HttpCookie cookie=cookiesAll.get(i);
            String cookieString = cookie.getName() + "=" + cookie.getValue() + "; path=/";// +cookie.getDomain();
            domainName=cookie.getDomain();
            cookieManager.setCookie(cookie.getDomain(), cookieString);
            if(xwalkCookieManager != null){
                xwalkCookieManager.setCookie(cookie.getDomain(), cookieString);
            }
            domainName="https://"+domainName;
            cookieManager.setCookie(domainName, cookieString);
            if(xwalkCookieManager != null){
                xwalkCookieManager.setCookie(domainName, cookieString);
            }
            LogUtil.d( domainName+"__"+cookieString);
        }

        LogUtil.d( "WebKit Cookies domain:"+domainName);
        String cookieString=cookieManager.getCookie(domainName);
        LogUtil.d("WebKit Cookies:"+cookieString);

        CookieSyncManager.getInstance().sync();
    }

    public static void clearWebViewCookies(Context context ){
        DbCookieStore instance = DbCookieStore.INSTANCE;
        instance.removeAll();
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookie();
        CookieSyncManager.getInstance().sync();
    }

    /**
     * 登出
     */
    public static void logout(Context context) {
        deleteAccessToken();
       clearWebViewCookies(context);
    }

    /**
     * 保存 AccessToken
     */
    private static void saveAccessToken(String accessToken) {
        SharedPreferences preferences = AppContext.getInstance().getSharedPreferences(PREF_FILE_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN_KEY, accessToken);
        editor.commit();
    }

    /**
     * 获取 AccessToken
     */
    public static String getAccessToken() {
        SharedPreferences preferences = AppContext.getInstance().getSharedPreferences(PREF_FILE_NAME, 0);
        String token = preferences.getString(TOKEN_KEY, null);
        return token;
    }

    private static void deleteAccessToken() {
        SharedPreferences preferences = AppContext.getInstance().getSharedPreferences(PREF_FILE_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

//    //   将所有请求参数除sign和图片等除外放入TreeMap
//    WebResourceRequest request = new WebResourceRequest;
//    TreeMap<String, String> params = new TreeMap<String, String>();
//    Enumeration<?> names = request.getParameterNames();
//    while (names.hasMoreElements()) {
//        String name = (String) names.nextElement();
//        if (!SIGN.equals(name)) {
//            String[] values = request.getParameterValues(name);
//            if (values != null) {
//                StringBuffer str = new StringBuffer(values[0]);
//                // 多值合并
//                for (int i = 1; i < values.length; i++) {
//                    str.append(name).append(values[i]);
//                }
//                params.put(name, str.toString());
//            }
//        }
//    }


}
