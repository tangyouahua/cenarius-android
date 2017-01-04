package com.m.cenarius.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.m.cenarius.Cenarius;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.utils.OpenApi;
import com.m.cenarius.view.CenariusWidget;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class LoginWidget implements CenariusWidget {

    public static final String TAG = CenariusWidget.class.getSimpleName();

    private static final String PREF_FILE_NAME = "cenarius_login";
    private static final String TOKEN_KEY = "access_token";

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
        params.put("terminalType", "mobile");
        params.put("rememberMe", "true");
        params.put("captchaId", captchaId);
        params.put("captcha", captcha);
        params.put("locale", "zh_CN");
        String sign = OpenApi.md5Signature(params, appSecret);
        params.put("sign", sign);

        RequestParams requestParams = new RequestParams(service);
        for (String key : params.keySet()) {
            requestParams.addQueryStringParameter(key, String.valueOf(params.get(key)));
        }

        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Map map = JSON.parseObject(result, Map.class);
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
     * 登出
     */
    public static void logout() {
        deleteAccessToken();
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
