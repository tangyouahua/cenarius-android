package com.m.cenarius.widget;

import android.app.Activity;
import android.webkit.WebView;

import com.m.cenarius.Cenarius;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.utils.LogUtils;
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
    static final String KEY_TITLE = "title";
    static final String TerminalType = "mobile";

    public interface LoginCallback {
        void onSuccess();

        void onFail();
    }

    @Override
    public String getPath() {
        return "/widget/login";
    }

    @Override
    public boolean handle(WebView view, String url) {
        HashMap dataMap = GsonHelper.getDataMap(url, getPath());
        if (dataMap != null) {
            if (null != view && view.getContext() instanceof Activity) {
                ((Activity) view.getContext()).setTitle((String) dataMap.get(KEY_TITLE));
            }
            return true;
        }
        return false;
    }

    public static void login(String username, String password, LoginCallback callback) {
        String service = Cenarius.LoginService;
        String appKey = Cenarius.LoginAppKey;
        String appSecret = Cenarius.LoginAppSecret;
        if (service == null || appKey == null || appSecret == null) {
            LogUtils.e(TAG, "先设置 service appKey appSecret");
            if (callback != null) {
                callback.onFail();
            }
            return;
        }

        TreeMap<String, String> params = new TreeMap<>();
        params.put("app_key", appKey);
        params.put("timestamp", Long.toString((new Date()).getTime()));
        params.put("username", username);
        params.put("password", password);
        params.put("terminalType", "mobile");
        params.put("rememberMe", "true");
        String sign = md5Signature(params, appSecret);
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

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    String result = response.body().string();

                }
            }
        });
    }

    private static String md5Signature(TreeMap<String, String> params, String secret) {
        String result = null;
        StringBuffer orgin = getBeforeSign(params, new StringBuffer(secret));
        if (orgin == null) {
            return result;
        }
        System.out.println("orgin:" + orgin);
        // secret last
        orgin.append(secret);
        System.out.println("orgin2:" + orgin);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            result = byte2hex(md.digest(orgin.toString().getBytes("utf-8")));
        } catch (Exception e) {
            throw new java.lang.RuntimeException("md5 sign error !", e);
        }
        return result;
    }

    private static StringBuffer getBeforeSign(TreeMap<String, String> params, StringBuffer orgin) {
        if (params == null) {
            return null;
        }

        Map<String, String> treeMap = new TreeMap<>();
        treeMap.putAll(params);
        Iterator<String> iter = treeMap.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            orgin.append(name).append(params.get(name));
        }
        return orgin;
    }

    private static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer();
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs.append("0").append(stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString().toUpperCase();
    }

}
