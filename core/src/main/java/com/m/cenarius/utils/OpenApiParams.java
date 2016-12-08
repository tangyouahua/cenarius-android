package com.m.cenarius.utils;

import android.net.Uri;

import org.xutils.http.RequestParams;
import org.xutils.http.app.ParamsBuilder;

/**
 * OpenApi 请求
 */

public class OpenApiParams extends RequestParams {

    public OpenApiParams() {
        this(null, null, null, null);
    }

    public OpenApiParams(String uri) {
        // 取出
//        Uri finalUri = Uri.parse(uri);
//        String baseUri = finalUri.getPath();
        this(uri, null, null, null);

    }

    public OpenApiParams(String uri, ParamsBuilder builder, String[] signs, String[] cacheKeys) {
//        super(uri, builder, signs, cacheKeys);
    }


}
