package com.m.cenarius.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.alibaba.fastjson.JSON;
import com.m.cenarius.Cenarius;
import com.m.cenarius.Constants;
import com.m.cenarius.resourceproxy.ResourceProxy;
import com.m.cenarius.resourceproxy.cache.AssetCache;
import com.m.cenarius.resourceproxy.cache.CacheEntry;
import com.m.cenarius.resourceproxy.cache.CacheHelper;
import com.m.cenarius.resourceproxy.cache.InternalCache;
import com.m.cenarius.resourceproxy.network.InterceptJavascriptInterface;
import com.m.cenarius.route.Route;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.MimeUtils;
import com.m.cenarius.utils.OpenApi;
import com.m.cenarius.utils.QueryUtil;
import com.m.cenarius.utils.Utils;
import com.m.cenarius.utils.io.IOUtils;

import org.apache.http.conn.ConnectTimeoutException;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.GzipSource;

/**
 * 处理拦截逻辑
 */

public class CenariusHandleRequest {

    public static boolean handleWidgets(View view, String url, List<CenariusWidget> widgets) {
        if (url.startsWith(Constants.CONTAINER_WIDGET_BASE)) {
            boolean handled;
            for (CenariusWidget widget : widgets) {
                if (null != widget) {
                    handled = widget.handle(view, url);
                    if (handled) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static WebResourceResponse handleResourceRequest(String requestUrl) {
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(requestUrl);
        String mimeType = MimeUtils.guessMimeTypeFromExtension(fileExtension);
        String uriString = uriForUrl(requestUrl);
        if (uriString != null) {
            // requestUrl 符合拦截规则
            Uri finalUri = Uri.parse(uriString);
            String baseUri = finalUri.getPath();
            RouteManager routeManager = RouteManager.getInstance();
            CacheEntry cacheEntry;
            if (routeManager.isInWhiteList(baseUri)) {
                // 白名单 缓存
                cacheEntry = AssetCache.getInstance().findWhiteListCache(baseUri);
                return new WebResourceResponse(mimeType, "UTF-8", cacheEntry.inputStream);
            } else {
                Route route = RouteManager.getInstance().findRoute(baseUri);
                if (route != null) {
                    // cache 缓存
                    cacheEntry = InternalCache.getInstance().findCache(route);
                    if (cacheEntry == null) {
                        // asset 缓存
                        cacheEntry = AssetCache.getInstance().findCache(route);
                    }
                    if (null != cacheEntry && cacheEntry.isValid()) {
                        return new WebResourceResponse(mimeType, "UTF-8", cacheEntry.inputStream);
                    }

                    // 从网络加载
                    try {
                        Log.v("cenarius", "start load h5 :" + requestUrl);
                        final PipedOutputStream out = new PipedOutputStream();
                        final PipedInputStream in = new PipedInputStream(out);
                        WebResourceResponse xResponse = new WebResourceResponse(mimeType, "UTF-8", in);
                        loadResourceRequest(route, out);
                        return xResponse;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("cenarius", "url : " + requestUrl + " " + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    public static WebResourceResponse handleAjaxRequest(String requestUrl, InterceptJavascriptInterface.AjaxRequestContents ajaxRequestContents) {
        // header
        Map header = JSON.parseObject(ajaxRequestContents.header, Map.class);
        if (header.get("X-Requested-With").equals("OpenAPIRequest")) {
            String query = Uri.parse(requestUrl).getQuery();
            if (query == null || QueryUtil.queryMap(query).get("sign") == null) {
                // 需要签名
                String body = ajaxRequestContents.body;
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(requestUrl);
                String mimeType = MimeUtils.guessMimeTypeFromExtension(fileExtension);
                // 从网络加载
                try {
                    Log.v("cenarius", "start load ajax :" + requestUrl);
                    final PipedOutputStream out = new PipedOutputStream();
                    final PipedInputStream in = new PipedInputStream(out);
                    WebResourceResponse xResponse = new WebResourceResponse(mimeType, "UTF-8", in);
                    // 把带参数的 uri 给到加载
                    final String url = OpenApi.openApiQuery(query, body);
                    loadAjaxRequest(ajaxRequestContents.method, requestUrl, ajaxRequestContents.header, ajaxRequestContents.body);
                    return xResponse;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("cenarius", "url : " + requestUrl + " " + e.getMessage());
                }
            }
        }


        return null;
    }

    private static void loadAjaxRequest(String method, String url, String header, String body) {
        method = method.toUpperCase();
        HttpMethod httpMethod;
        if (method.equals("DELETE")) {
            httpMethod = HttpMethod.DELETE;
        } else if (method.equals("POST")) {
            httpMethod = HttpMethod.POST;
        } else if (method.equals("PUT")) {
            httpMethod = HttpMethod.PUT;
        } else {
            httpMethod = HttpMethod.GET;
        }
        RequestParams requestParams = new RequestParams(url);
        if (header != null){
            
        }

        x.http().request(httpMethod, requestParams, new Callback.CommonCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private static void loadResourceRequest(final Route route, final PipedOutputStream outputStream) {

        RequestParams requestParams = new RequestParams(route.getHtmlFile());
        x.http().get(requestParams, new Callback.CommonCallback<byte[]>() {

            @Override
            public void onSuccess(byte[] result) {
                try {
                    outputStream.write(result);
                    InternalCache.getInstance().saveCache(route, result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }

    public static String uriForUrl(String url) {
        String uri;
        //HTTP
        String remoteFolderUrl = RouteManager.getInstance().remoteFolderUrl + "/";
        uri = deleteString(remoteFolderUrl, url);
        if (uri != null) {
            return uri;
        }
        //cache
        String cachePath = "file://" + InternalCache.getInstance().cachePath() + Constants.DEFAULT_ASSET_FILE_PATH + "/";
        uri = deleteString(cachePath, url);
        if (uri != null) {
            return uri;
        }
        //resource
        String assetsPath = AssetCache.getInstance().assetsPath() + Constants.DEFAULT_ASSET_FILE_PATH + "/";
        uri = deleteString(assetsPath, url);
        if (uri != null) {
            return uri;
        }
        return null;
    }

    private static String deleteString(String deleteString, String fromString) {
        if (fromString.startsWith(deleteString)) {
            return fromString.replace(deleteString, "");
        }
        return null;
    }

    /**
     * 是否是html文档
     *
     * @param requestUrl
     * @return
     */
    public static boolean isHtmlResource(String requestUrl) {
        if (TextUtils.isEmpty(requestUrl)) {
            return false;
        }
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(requestUrl);
        return TextUtils.equals(fileExtension, Constants.EXTENSION_HTML)
                || TextUtils.equals(fileExtension, Constants.EXTENSION_HTM);
    }

    /**
     * 是否是js文档
     *
     * @param requestUrl
     * @return
     */
    public static boolean isJsResource(String requestUrl) {
        if (TextUtils.isEmpty(requestUrl)) {
            return false;
        }
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(requestUrl);
        return TextUtils.equals(fileExtension, Constants.EXTENSION_JS);
    }

}
