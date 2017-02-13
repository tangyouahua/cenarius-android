package com.m.cenarius.utils;


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttpClientHelper {

    private static OkHttpClient defaultClient;

    public static OkHttpClient getDefaultClient() {
        if (defaultClient == null) {
            defaultClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
        }

        return defaultClient;
    }

}
