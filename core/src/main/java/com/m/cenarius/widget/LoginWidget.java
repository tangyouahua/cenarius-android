package com.m.cenarius.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.m.cenarius.Cenarius;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.utils.OpenApi;
import com.m.cenarius.view.CenariusWidget;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    public static void login(final Context context, String username, String password, final LoginCallback callback) {
        String service = Cenarius.LoginService;
        String appKey = Cenarius.LoginAppKey;
        String appSecret = Cenarius.LoginAppSecret;
        if (service == null || appKey == null || appSecret == null) {
            if (callback != null) {
                callback.onFail("先设置 service appKey appSecret");
            }
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("app_key", appKey);
        params.put("timestamp", Long.toString((new Date()).getTime()));
        params.put("username", username);
        params.put("password", password);
        params.put("terminalType", "mobile");
        params.put("rememberMe", "true");
        String sign = OpenApi.md5Signature(params, appSecret);
        params.put("sign", sign);

        OkHttpClient client = Cenarius.getOkHttpClient();
        FormBody.Builder builder = new FormBody.Builder();
        Iterator<String> iter = params.keySet().iterator();
        String key;
        while (iter.hasNext()) {
            key = iter.next();
            builder.add(key, String.valueOf(params.get(key)));
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder()
                .url(service)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFail("系统错误");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String dataJson = response.body().string();
                    HashMap dataMap = JSON.parseObject(dataJson, HashMap.class);
                    String token = (String) dataMap.get("access_token");
                    String error_msg = (String) dataMap.get("error_msg");
                    if (token != null && token.length() > 0) {
                        saveAccessToken(token, context);
                        if (callback != null) {
                            callback.onSuccess(token);
                        }
                    } else {
                        if (callback != null) {
                            if (error_msg != null && error_msg.length() > 0){
                                callback.onFail(error_msg);
                            }
                            else {
                                callback.onFail("系统错误");
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 登出
     */
    public static void logout(final Context context){
        deleteAccessToken(context);
    }

    /**
     * 保存 AccessToken
     */
    private static void saveAccessToken(String accessToken, final Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN_KEY, accessToken);
        editor.commit();
    }

    /**
     * 获取 AccessToken
     */
    public static String getAccessToken(final Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, 0);
        String token = preferences.getString(TOKEN_KEY, null);
        return token;
    }

    private static void deleteAccessToken(final Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, 0);
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
