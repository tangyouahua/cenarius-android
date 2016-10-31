package com.m.cenarius.example;

import android.text.TextUtils;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor{

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        String url = request.url().toString();
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        Request.Builder builder = request.newBuilder();
        builder.header("Authorization", "123456789");
        return chain.proceed(builder.build());
    }
}
