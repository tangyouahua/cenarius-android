package com.m.cenarius.widget;

import android.app.Activity;
import android.webkit.WebView;

import com.m.cenarius.Cenarius;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.view.CenariusWidget;

import java.io.IOException;
import java.util.HashMap;

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
        if (service == null || appKey == null || appSecret == null){
            LogUtils.e(TAG, "先设置 service appKey appSecret");
            if (callback != null){
                callback.onFail();
            }
            return;
        }

        OkHttpClient client = Cenarius.getOkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("app_key",appKey)
                .add("timestamp", "")
                .add("username", username)
                .add("password", password)
                .add("terminalType", TerminalType)
                .add("rememberMe", "true")
                .build();
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

            }
        });
    }
}
