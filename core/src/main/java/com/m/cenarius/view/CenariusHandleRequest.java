package com.m.cenarius.view;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.m.cenarius.Cenarius;
import com.m.cenarius.Constants;
import com.m.cenarius.resourceproxy.ResourceProxy;
import com.m.cenarius.resourceproxy.cache.AssetCache;
import com.m.cenarius.resourceproxy.cache.CacheEntry;
import com.m.cenarius.resourceproxy.cache.CacheHelper;
import com.m.cenarius.resourceproxy.cache.InternalCache;
import com.m.cenarius.route.Route;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.MimeUtils;
import com.m.cenarius.utils.Utils;
import com.m.cenarius.utils.io.IOUtils;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;
import org.xutils.common.util.LogUtil;

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

    public static WebResourceResponse handleResourceRequest(View webView, String requestUrl) {
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
            } else if (routeManager.isInRoutes(baseUri)) {
                Route route = RouteManager.getInstance().findRoute(baseUri);
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
                    LogUtil.v("start load async :" + requestUrl);
                    final PipedOutputStream out = new PipedOutputStream();
                    final PipedInputStream in = new PipedInputStream(out);
                    WebResourceResponse xResponse = new WebResourceResponse(mimeType, "UTF-8", in);
                    if (Utils.hasLollipop()) {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Access-Control-Allow-Origin", "*");
                        xResponse.setResponseHeaders(headers);
                    }
                    // 把带参数的 uri 给到加载
                    final String url = uriString;
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            new Thread(new ResourceRequest(url, out, in)).start();
                        }
                    });
                    return xResponse;
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtil.e("url : " + requestUrl + " " + e.getMessage());
                } catch (Throwable e) {
                    e.printStackTrace();
                    LogUtil.e("url : " + requestUrl + " " + e.getMessage());
                }
            }
        }

        return null;
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

    /**
     * 构建网络请求
     *
     * @param requestUrl
     * @return
     */
    public static Request buildRequest(String requestUrl) {
        if (TextUtils.isEmpty(requestUrl)) {
            return null;
        }
        Request.Builder builder = new Request.Builder()
                .url(requestUrl);
        Uri uri = Uri.parse(requestUrl);
        String method = uri.getQueryParameter(Constants.KEY_METHOD);
        //  如果没有值则视为get
        if (Constants.METHOD_POST.equalsIgnoreCase(method)) {
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            Set<String> names = uri.getQueryParameterNames();
            for (String key : names) {
                formBodyBuilder.add(key, uri.getQueryParameter(key));
            }
            builder.method("POST", formBodyBuilder.build());
        } else {
            builder.method("GET", null);
        }
        builder.addHeader("User-Agent", Cenarius.getUserAgent());
        return builder.build();
    }

    /**
     * html或js加载错误，页面无法渲染，通知{@link CenariusWebView}显示错误界面，重新加载
     *
     * @param errorType 错误类型
     */
    public static void showError(int errorType) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.KEY_ERROR_TYPE, errorType);
        BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.EVENT_CNRS_NETWORK_ERROR, bundle));
    }

}

/**
 * 先返回一个空的InputStream，然后再通过异步的方式向里面写数据。
 */
class ResourceRequest implements Runnable {

    // 请求地址
    String mUrl;
    // 输出流
    PipedOutputStream mOut;
    // 输入流
    PipedInputStream mTarget;

    public ResourceRequest(String url, PipedOutputStream outputStream, PipedInputStream target) {
        this.mUrl = url;
        this.mOut = outputStream;
        this.mTarget = target;
    }

    @Override
    public void run() {
        try {
            CacheEntry cacheEntry = null;
            Uri finalUri = Uri.parse(mUrl);
            String baseUri = finalUri.getPath();
            Route route = RouteManager.getInstance().findRoute(baseUri);
//            // cache 缓存
//            cacheEntry = InternalCache.getInstance().findCache(route);
//            if (cacheEntry == null) {
//                // asset 缓存
//                cacheEntry = AssetCache.getInstance().findCache(route);
//                if (cacheEntry == null){
//                    // 白名单 缓存
//                    cacheEntry = AssetCache.getInstance().findWhiteListCache(baseUri);
//                }
//            }
//            if (null != cacheEntry && cacheEntry.isValid()) {
//                byte[] bytes = IOUtils.toByteArray(cacheEntry.inputStream);
//                mOut.write(bytes);
//                return;
//            }

            // 从网络加载
            String remoteHtmlURL = CacheHelper.getInstance().remoteHtmlURLForURI(mUrl);
            if (remoteHtmlURL == null) {
                return;
            }
            Response response = ResourceProxy.getInstance().getNetwork()
                    .handle(CenariusHandleRequest.buildRequest(remoteHtmlURL));
            // 写缓存
            if (response.isSuccessful()) {
                InputStream inputStream = null;
                if (null != response.body()) {
                    InternalCache.getInstance().saveCache(route, IOUtils.toByteArray(response.body().byteStream()));
                    cacheEntry = InternalCache.getInstance().findCache(route);
                    if (null != cacheEntry && cacheEntry.isValid()) {
                        inputStream = cacheEntry.inputStream;
                    }
                }
                if (null == inputStream && null != response.body()) {
                    inputStream = response.body().byteStream();
                }
                // 正常输出
                if (null != inputStream) {
                    mOut.write(IOUtils.toByteArray(inputStream));
                }
            } else {
                // 输出错误
                byte[] result = wrapperErrorResponse(response);
                try {
                    mOut.write(result);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (SocketTimeoutException e) {
            try {
                // 输出错误
                byte[] result = wrapperErrorResponse(e);
                mOut.write(result);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (ConnectTimeoutException e) {
            // 输出错误
            byte[] result = wrapperErrorResponse(e);
            try {
                mOut.write(result);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 输出错误
            byte[] result = wrapperErrorResponse(e);
            try {
                mOut.write(result);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                mOut.flush();
                mOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean responseGzip(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey()
                    .toLowerCase()
                    .equals(Constants.HEADER_CONTENT_ENCODING.toLowerCase())
                    && entry.getValue()
                    .toLowerCase()
                    .equals(Constants.ENCODING_GZIP.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private byte[] parseGzipResponseBody(ResponseBody body) throws IOException {
        Buffer buffer = new Buffer();
        GzipSource gzipSource = new GzipSource(body.source());
        while (gzipSource.read(buffer, Integer.MAX_VALUE) != -1) {
        }
        gzipSource.close();
        return buffer.readByteArray();
    }

    private byte[] wrapperErrorResponse(Exception exception) {
//        if (null == exception) {
//            return new byte[0];
//        }
//
//        try {
//            // generate json response
//            JSONObject result = new JSONObject();
//            result.put(Constants.KEY_NETWORK_ERROR, true);
//            return (Constants.ERROR_PREFIX + result.toString()).getBytes();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return new byte[0];
    }

    private byte[] wrapperErrorResponse(Response response) {
//        if (null == response) {
//            return new byte[0];
//        }
//        try {
//            // read response content
//            Map<String, String> responseHeaders = new HashMap<>();
//            for (String field : response.headers()
//                    .names()) {
//                responseHeaders.put(field, response.headers()
//                        .get(field));
//            }
//            byte[] responseContents = new byte[0];
//            if (null != response.body()) {
//                if (responseGzip(responseHeaders)) {
//                    responseContents = parseGzipResponseBody(response.body());
//                } else {
//                    responseContents = response.body().bytes();
//                }
//            }
//
//            // generate json response
//            JSONObject result = new JSONObject();
//            result.put(Constants.KEY_RESPONSE_CODE, response.code());
//            String apiError = new String(responseContents, "utf-8");
//            try {
//                JSONObject content = new JSONObject(apiError);
//                result.put(Constants.KEY_RESPONSE_ERROR, content);
//            } catch (Exception e) {
//                e.printStackTrace();
//                result.put(Constants.KEY_RESPONSE_ERROR, apiError);
//            }
//            return (Constants.ERROR_PREFIX + result.toString()).getBytes();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return new byte[0];
    }
}
