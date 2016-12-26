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
        List<RequestParams.Header> headers = requestParams.getHeaders();
        boolean isOpenApi = false;
        if (headers != null) {
            for (RequestParams.Header header : headers) {
                if ("X-Requested-With".equals(header.key) && "OpenAPIRequest".equals(header.value)) {
                    isOpenApi = true;
                    break;
                }
            }
        }
        if (isOpenApi) {
            // 签名
            OpenApi.openApiForRequestParams(requestParams);
        }
    }
}
