package com.m.cenarius.utils;

import org.xutils.http.RequestParams;
import org.xutils.http.app.RequestTracker;
import org.xutils.http.request.UriRequest;

import java.util.List;

/**
 * OpenApi 拦截器
 */

public class OpenApiTracker implements RequestTracker {
    @Override
    public void onWaiting(RequestParams params) {

    }

    @Override
    public void onStart(RequestParams params) {
        List<RequestParams.Header> headers = params.getHeaders();
        boolean isOpenApi = false;
        if (headers != null) {
            for (RequestParams.Header header : headers) {
                if (header.key.equals("X-Requested-With") && header.value.equals("OpenAPIRequest")) {
                    isOpenApi = true;
                    break;
                }
            }
        }
        if (isOpenApi) {
            // 签名
            OpenApi.openApiForRequestParams(params);
        }
    }

    @Override
    public void onRequestCreated(UriRequest request) {

    }

    @Override
    public void onCache(UriRequest request, Object result) {

    }

    @Override
    public void onSuccess(UriRequest request, Object result) {

    }

    @Override
    public void onCancelled(UriRequest request) {

    }

    @Override
    public void onError(UriRequest request, Throwable ex, boolean isCallbackError) {

    }

    @Override
    public void onFinished(UriRequest request) {

    }
}
