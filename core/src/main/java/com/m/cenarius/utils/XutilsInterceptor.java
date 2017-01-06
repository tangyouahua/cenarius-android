package com.m.cenarius.utils;

import org.xutils.http.RequestParams;

import java.util.List;

/**
 * Xutils 的拦截器
 */
public class XutilsInterceptor {

    /**
     * 签名
     */
    public static void openApiForRequestParams(RequestParams requestParams){
        OpenApi.openApiForRequestParams(requestParams);
    }
}
