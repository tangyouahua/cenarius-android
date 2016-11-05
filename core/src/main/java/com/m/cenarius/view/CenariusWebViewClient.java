package com.m.cenarius.view;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.m.cenarius.Constants;
import com.m.cenarius.Cenarius;
import com.m.cenarius.resourceproxy.ResourceProxy;
import com.m.cenarius.resourceproxy.cache.AssetCache;
import com.m.cenarius.resourceproxy.cache.CacheEntry;
import com.m.cenarius.resourceproxy.cache.CacheHelper;
import com.m.cenarius.route.Route;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.utils.MimeUtils;
import com.m.cenarius.utils.Utils;
import com.m.cenarius.utils.io.IOUtils;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
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


public class CenariusWebViewClient extends WebViewClient {

    static final String TAG = CenariusWebViewClient.class.getSimpleName();

    private List<CenariusWidget> mWidgets = new ArrayList<>();

    /**
     * 自定义url拦截处理
     *
     * @param widget
     */
    public void addCenariusWidget(CenariusWidget widget) {
        if (null != widget) {
            mWidgets.add(widget);
        }
    }

    public List<CenariusWidget> getCenariusWidgets() {
        return mWidgets;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        LogUtils.i(TAG, "[shouldOverrideUrlLoading] : url = " + url);
        if (url.startsWith(Constants.CONTAINER_WIDGET_BASE)) {
            boolean handled;
            for (CenariusWidget widget : mWidgets) {
                if (null != widget) {
                    handled = widget.handle(view, url);
                    if (handled) {
                        return true;
                    }
                }
            }
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (Utils.hasLollipop()) {
            return handleResourceRequest(view, request.getUrl().toString());
        } else {
            return super.shouldInterceptRequest(view, request);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return handleResourceRequest(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        LogUtils.i(TAG, "onPageStarted");
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        LogUtils.i(TAG, "onPageFinished");
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        LogUtils.i(TAG, "onLoadResource : " + url);
    }

    /**
     * 拦截资源请求，部分资源需要返回本地资源
     * <p>
     * <p>
     * html，js资源直接渲染进程返回,图片等其他资源先返回空的数据流再异步向流中写数据
     * <p>
     * <p>
     * <note>这个方法会在渲染线程执行，如果做了耗时操作会block渲染</note>
     */
    private WebResourceResponse handleResourceRequest(WebView webView, String requestUrl) {
        LogUtils.i(TAG, "[handleResourceRequest] url =  " + requestUrl);
        String uriString = uriForUrl(requestUrl);
        Uri finalUri = Uri.parse(uriString);
        String baseUri = finalUri.getPath();
        Route route = RouteManager.getInstance().findRoute(baseUri);
        if (route == null) {
            return super.shouldInterceptRequest(webView, requestUrl);
        }

        String htmlFileURL = CacheHelper.getInstance().localHtmlURLForURI(uriString);
        if (htmlFileURL == null) {
            htmlFileURL = CacheHelper.getInstance().remoteHtmlURLForURI(uriString);
        }
        return super.shouldInterceptRequest(webView, htmlFileURL);
    }

        // html js 直接返回
//        if (Helper.isHtmlResource(requestUrl) || Helper.isJsResource(requestUrl)) {
//            String htmlFileURL = CacheHelper.getInstance().localHtmlURLForURI(uriString);
//            if (htmlFileURL == null) {
//                htmlFileURL = CacheHelper.getInstance().remoteHtmlURLForURI(uriString);
//            }
//            return super.shouldInterceptRequest(webView, htmlFileURL);
//
//
//            final CacheEntry cacheEntry = CacheHelper.getInstance().findCache(route);
//            if (null == cacheEntry) {
//                // 没有cache，
//                return super.shouldInterceptRequest(webView, requestUrl);
//            }
//            if (!cacheEntry.isValid()) {
//                // 有cache但无效，清除缓存
//                CacheHelper.getInstance().removeCache(route);
//                return super.shouldInterceptRequest(webView, requestUrl);
//            } else {
//                //读缓存
//                LogUtils.i(TAG, "cache hit :" + requestUrl);
//                String data = "";
//                try {
//                    data = IOUtils.toString(cacheEntry.inputStream);
//                    // hack 检查cache是否完整
//                    if (TextUtils.isEmpty(data)) {
//                        CacheHelper.getInstance().removeCache(route);
//                        return super.shouldInterceptRequest(webView, requestUrl);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    // hack 检查cache是否完整
//                    CacheHelper.getInstance().removeCache(route);
//                    return super.shouldInterceptRequest(webView, requestUrl);
//                }
//                return new WebResourceResponse(Constants.MIME_TYPE_HTML, "utf-8", IOUtils.toInputStream(data));
//            }
//        }
//
//        // js直接返回
//        if (Helper.isJsResource(requestUrl)) {
//            final CacheEntry cacheEntry = CacheHelper.getInstance().findCache(route);
//            if (null == cacheEntry) {
//                // 后面逻辑会通过network去加载
//                // 加载后再显示
//            } else if (!cacheEntry.isValid()) {
//                // 后面逻辑会通过network去加载
//                // 加载后再显示
//                // 清除缓存
//                CacheHelper.getInstance().removeCache(route);
//            } else {
//                String data = "";
//                try {
//                    data = IOUtils.toString(cacheEntry.inputStream);
//                    if (TextUtils.isEmpty(data) || (cacheEntry.length > 0 && cacheEntry.length != data.length())) {
//                        CacheHelper.getInstance().removeCache(route);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    CacheHelper.getInstance().removeCache(route);
//                }
//                LogUtils.i(TAG, "cache hit :" + requestUrl);
//                return new WebResourceResponse(Constants.MIME_TYPE_HTML, "utf-8", IOUtils.toInputStream(data));
//            }
//        }
//
//        // 图片等其他资源使用先返回空流，异步写数据
//        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(requestUrl);
//        String mimeType = MimeUtils.guessMimeTypeFromExtension(fileExtension);
//        try {
//            LogUtils.i(TAG, "start load async :" + requestUrl);
//            final PipedOutputStream out = new PipedOutputStream();
//            final PipedInputStream in = new PipedInputStream(out);
//            WebResourceResponse xResponse = new WebResourceResponse(mimeType, "UTF-8", in);
//            if (Utils.hasLollipop()) {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Access-Control-Allow-Origin", "*");
//                xResponse.setResponseHeaders(headers);
//            }
//            final String url = requestUrl;
//            webView.post(new Runnable() {
//                @Override
//                public void run() {
//                    new Thread(new ResourceRequest(url, out, in)).start();
//                }
//            });
//            return xResponse;
//        } catch (IOException e) {
//            e.printStackTrace();
//            LogUtils.e(TAG, "url : " + requestUrl + " " + e.getMessage());
//            return super.shouldInterceptRequest(webView, requestUrl);
//        } catch (Throwable e) {
//            e.printStackTrace();
//            LogUtils.e(TAG, "url : " + requestUrl + " " + e.getMessage());
//            return super.shouldInterceptRequest(webView, requestUrl);
//        }
//    }

    /**
     * html或js加载错误，页面无法渲染，通知{@link CenariusWebView}显示错误界面，重新加载
     *
     * @param errorType 错误类型
     */
    public void showError(int errorType) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.KEY_ERROR_TYPE, errorType);
        BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.EVENT_CNRS_NETWORK_ERROR, bundle));
    }

//    /**
//     * @param requestUrl
//     * @return
//     */
//    private boolean shouldIntercept(String requestUrl) {
//        String uri = uriForUrl(requestUrl);
//        if (uri != null) {
//            return true;
//        }
//        return false;
//    }

//        Route route = RouteManager.getInstance().findRoute(requestUrl);
//        if (route != null){
//            return true;
//        }

//        if (TextUtils.isEmpty(requestUrl)) {
//            return false;
//        }
//        // file协议需要替换,用于html
//        if (requestUrl.startsWith(Constants.FILE_AUTHORITY)) {
//            return true;
//        }
//
//        // cenarius container api，需要拦截
//        if (requestUrl.startsWith(Constants.CONTAINER_API_BASE)) {
//            return true;
//        }
//
//        // 非合法uri，不拦截
//        Uri uri = null;
//        try {
//            uri = Uri.parse(requestUrl);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (null == uri) {
//            return false;
//        }
//
//        // 非合法host，不拦截
//        String host = uri.getHost();
//        if (TextUtils.isEmpty(host)) {
//            return false;
//        }
//
//        // 不能拦截的uri，不拦截
//        Pattern pattern;
//        Matcher matcher;
//        for (String interceptHostItem : ResourceProxy.getInstance().getProxyHosts()) {
//            pattern = Pattern.compile(interceptHostItem);
//            matcher = pattern.matcher(host);
//            if (matcher.find()) {
//                return true;
//            }
//        }
//        return false;
//    }

    private String uriForUrl(String url) {
        String uri;
        //HTTP
        String remoteFolderUrl = RouteManager.getInstance().remoteFolderUrl + "/";
        uri = deleteString(remoteFolderUrl, url);
        if (uri != null) {
            return uri;
        }
        //cache
        String cachePath = "file://" + CacheHelper.getInstance().cachePath();
        uri = deleteString(cachePath, url);
        if (uri != null) {
            return uri;
        }
        //resource
        String assetsPath = AssetCache.getInstance().assetsPath();
        uri = deleteString(assetsPath, url);
        if (uri != null) {
            return uri;
        }
        return null;
    }

    private String deleteString(String deleteString, String fromString) {
        if (fromString.startsWith(deleteString)) {
            return fromString.replace(deleteString, "");
        }
        return null;
    }


    private static class Helper {

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

    }


    /**
     * {@link #shouldInterceptRequest(WebView, String)} 异步拦截
     * <p>
     * 先返回一个空的InputStream，然后再通过异步的方式向里面写数据。
     */
    private class ResourceRequest implements Runnable {

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
                // read cache first
                CacheEntry cacheEntry = null;
                String uri = uriForUrl(mUrl);
                Route route = RouteManager.getInstance().findRoute(uri);
                if (CacheHelper.getInstance().cacheEnabled()) {
                    cacheEntry = CacheHelper.getInstance().findCache(route);
                }
                if (null != cacheEntry && cacheEntry.isValid()) {
                    byte[] bytes = IOUtils.toByteArray(cacheEntry.inputStream);
                    LogUtils.i(TAG, "load async cache hit :" + mUrl);
                    mOut.write(bytes);
                    return;
                }

                // request network
                Response response = ResourceProxy.getInstance().getNetwork()
                        .handle(Helper.buildRequest(mUrl));
                // write cache
                if (response.isSuccessful()) {
                    InputStream inputStream = null;
                    if (null != uri && null != response.body()) {
                        CacheHelper.getInstance().saveCache(route, IOUtils.toByteArray(response.body().byteStream()));
                        cacheEntry = CacheHelper.getInstance().findCache(route);
                        if (null != cacheEntry && cacheEntry.isValid()) {
                            inputStream = cacheEntry.inputStream;
                        }
                    }
                    if (null == inputStream && null != response.body()) {
                        inputStream = response.body().byteStream();
                    }
                    // write output
                    if (null != inputStream) {
                        mOut.write(IOUtils.toByteArray(inputStream));
                        LogUtils.i(TAG, "load async completed :" + mUrl);
                    }
                } else {
                    LogUtils.i(TAG, "load async failed :" + mUrl);
                    if (Helper.isJsResource(mUrl)) {
                        showError(CenariusWebViewCore.RxLoadError.JS_CACHE_INVALID.type);
                        return;
                    }

                    // return request error
                    byte[] result = wrapperErrorResponse(response);
                    if (Cenarius.DEBUG) {
                        LogUtils.i(TAG, "Api Error: " + new String(result));
                    }
                    try {
                        mOut.write(result);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (SocketTimeoutException e) {
                try {
                    byte[] result = wrapperErrorResponse(e);
                    if (Cenarius.DEBUG) {
                        LogUtils.i(TAG, "SocketTimeoutException: " + new String(result));
                    }
                    mOut.write(result);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (ConnectTimeoutException e) {
                byte[] result = wrapperErrorResponse(e);
                if (Cenarius.DEBUG) {
                    LogUtils.i(TAG, "ConnectTimeoutException: " + new String(result));
                }
                try {
                    mOut.write(result);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.i(TAG, "load async exception :" + mUrl + " ; " + e.getMessage());
                if (Helper.isJsResource(mUrl)) {
                    showError(CenariusWebViewCore.RxLoadError.JS_CACHE_INVALID.type);
                    return;
                }
                byte[] result = wrapperErrorResponse(e);
                if (Cenarius.DEBUG) {
                    LogUtils.i(TAG, "Exception: " + new String(result));
                }
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
            if (null == exception) {
                return new byte[0];
            }

            try {
                // generate json response
                JSONObject result = new JSONObject();
                result.put(Constants.KEY_NETWORK_ERROR, true);
                return (Constants.ERROR_PREFIX + result.toString()).getBytes();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new byte[0];
        }

        private byte[] wrapperErrorResponse(Response response) {
            if (null == response) {
                return new byte[0];
            }
            try {
                // read response content
                Map<String, String> responseHeaders = new HashMap<>();
                for (String field : response.headers()
                        .names()) {
                    responseHeaders.put(field, response.headers()
                            .get(field));
                }
                byte[] responseContents = new byte[0];
                if (null != response.body()) {
                    if (responseGzip(responseHeaders)) {
                        responseContents = parseGzipResponseBody(response.body());
                    } else {
                        responseContents = response.body().bytes();
                    }
                }

                // generate json response
                JSONObject result = new JSONObject();
                result.put(Constants.KEY_RESPONSE_CODE, response.code());
                String apiError = new String(responseContents, "utf-8");
                try {
                    JSONObject content = new JSONObject(apiError);
                    result.put(Constants.KEY_RESPONSE_ERROR, content);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.put(Constants.KEY_RESPONSE_ERROR, apiError);
                }
                return (Constants.ERROR_PREFIX + result.toString()).getBytes();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new byte[0];
        }
    }
}
