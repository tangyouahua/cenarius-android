package org.apache.cordova.cordovautil.XWalkCordova;

import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.m.cenarius.Cenarius;
import com.m.cenarius.Constants;
import com.m.cenarius.resourceproxy.ResourceProxy;
import com.m.cenarius.resourceproxy.cache.AssetCache;
import com.m.cenarius.resourceproxy.cache.CacheEntry;
import com.m.cenarius.resourceproxy.cache.CacheHelper;
import com.m.cenarius.route.Route;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.Utils;
import com.m.cenarius.utils.io.IOUtils;
import com.m.cenarius.view.CenariusWebView;
import com.m.cenarius.view.CenariusWebViewCore;

import org.apache.cordova.cordovautil.SystemCordova.CordovaBaseWebViewClient;
import org.apache.cordova.cordovautil.WebViewClientListener;
//import org.apache.cordova.BuildConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.crosswalk.engine.XWalkCordovaResourceClient;
import org.crosswalk.engine.XWalkWebViewEngine;
import org.json.JSONObject;
import org.xutils.common.util.LogUtil;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.GzipSource;

/**
 * ********************************************
 *
 * @Description 网络的拦截处理器（XWalk内核的）
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 10:37
 * *********************************************
 */
public class XWalkBaseWebViewClient extends XWalkCordovaResourceClient {

    WebViewClientListener listener;

    public XWalkBaseWebViewClient(XWalkWebViewEngine parentEngine) {
        super(parentEngine);
    }

    public void setWebViewClientListener(WebViewClientListener listener) {
        this.listener=listener;
    }

    @Override
    public void onLoadStarted(XWalkView view, String url) {
        super.onLoadStarted(view, url);
        LogUtil.v("此时的onPageStarted: "+url);
        if (listener!=null){
            listener.onPageStarted(view,url,null);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
        LogUtil.v("此时的shouldOverrideUrlLoading: "+url);
        //此处可做拦截处理

        int isReturn=0;// 0代表返回默认的super方法,  1代表返回true ,2代表返回false
        if (listener!=null) {
            isReturn= listener.shouldOverrideUrlLoading(view, url);
        }
        if (isReturn==1) {
            return true;
        }else if(isReturn==2){
            return false;
        }else{
            return super.shouldOverrideUrlLoading(view, url);
        }

    }


    @Override
    public void onLoadFinished(XWalkView view, String url) {
        super.onLoadFinished(view, url);
        LogUtil.v("此时的onPageFinished: "+url);
        if (listener!=null){
            listener.onPageFinished(view,url);
        }
    }



    @Override
    public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
        super.onReceivedLoadError(view, errorCode, description, failingUrl);
        LogUtil.v("此时的onReceivedError:  errorCode--> "
                +errorCode+"  description--> "+description+"  failingUrl--> "+failingUrl);
        //此处可做网络异常的处理
        if (listener!=null){
            listener.onReceivedError(view,errorCode,description,failingUrl);
        }
    }


    @Override
    public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
        super.onReceivedSslError(view, callback, error);
        LogUtil.v("此时的shouldOverrideUrlLoading: "+error!=null?error.getUrl():" 链接为空");
//        if (BuildConfig.DEBUG){
//            callback.onReceiveValue(true);// debug = true
//        }else{
//            callback.onReceiveValue(false);// debug = false
//        }
    }

    @Override
    public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {

        return handleResourceRequest(view, url);
    }

    @Override
    public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
        if (Utils.hasLollipop()) {
            return handleResourceRequest(view, request);
        } else {
            return super.shouldInterceptLoadRequest(view, request);
        }
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
    private XWalkWebResourceResponse handleResourceRequest(XWalkView webView,XWalkWebResourceRequest request) {
        LogUtil.i( "[handleResourceRequest] url =  " + request.getUrl().toString());
        String uriString = uriForUrl(request.getUrl().toString());
        Uri finalUri = Uri.parse(uriString);
        String baseUri = finalUri.getPath();
        Route route = RouteManager.getInstance().findRoute(baseUri);
        if (route == null) {
            return super.shouldInterceptLoadRequest(webView, request);
        }

        String htmlFileURL = CacheHelper.getInstance().localHtmlURLForURI(uriString);
        if (htmlFileURL == null) {
            htmlFileURL = CacheHelper.getInstance().remoteHtmlURLForURI(uriString);
        }
        return super.shouldInterceptLoadRequest(webView, htmlFileURL);
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
    private WebResourceResponse handleResourceRequest(XWalkView webView, String requestUrl) {
        LogUtil.i( "[handleResourceRequest] url =  " + requestUrl);
        String uriString = uriForUrl(requestUrl);
        Uri finalUri = Uri.parse(uriString);
        String baseUri = finalUri.getPath();
        Route route = RouteManager.getInstance().findRoute(baseUri);
        if (route == null) {
            return super.shouldInterceptLoadRequest(webView, requestUrl);
        }

        String htmlFileURL = CacheHelper.getInstance().localHtmlURLForURI(uriString);
        if (htmlFileURL == null) {
            htmlFileURL = CacheHelper.getInstance().remoteHtmlURLForURI(uriString);
        }
        return super.shouldInterceptLoadRequest(webView, htmlFileURL);
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
//                LogUtil.i( "cache hit :" + requestUrl);
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
//                LogUtil.i( "cache hit :" + requestUrl);
//                return new WebResourceResponse(Constants.MIME_TYPE_HTML, "utf-8", IOUtils.toInputStream(data));
//            }
//        }
//
//        // 图片等其他资源使用先返回空流，异步写数据
//        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(requestUrl);
//        String mimeType = MimeUtils.guessMimeTypeFromExtension(fileExtension);
//        try {
//            LogUtil.i( "start load async :" + requestUrl);
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
                    LogUtil.i( "load async cache hit :" + mUrl);
                    mOut.write(bytes);
                    return;
                }

                // request network
                Response response = ResourceProxy.getInstance().getNetwork()
                        .handle(XWalkBaseWebViewClient.Helper.buildRequest(mUrl));
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
                        LogUtil.i( "load async completed :" + mUrl);
                    }
                } else {
                    LogUtil.i("load async failed :" + mUrl);
                    if (XWalkBaseWebViewClient.Helper.isJsResource(mUrl)) {
                        showError(CenariusWebViewCore.RxLoadError.JS_CACHE_INVALID.type);
                        return;
                    }

                    // return request error
                    byte[] result = wrapperErrorResponse(response);
                    if (Cenarius.DEBUG) {
                        LogUtil.i( "Api Error: " + new String(result));
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
                        LogUtil.i( "SocketTimeoutException: " + new String(result));
                    }
                    mOut.write(result);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (ConnectTimeoutException e) {
                byte[] result = wrapperErrorResponse(e);
                if (Cenarius.DEBUG) {
                    LogUtil.i( "ConnectTimeoutException: " + new String(result));
                }
                try {
                    mOut.write(result);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i( "load async exception :" + mUrl + " ; " + e.getMessage());
                if (XWalkBaseWebViewClient.Helper.isJsResource(mUrl)) {
                    showError(CenariusWebViewCore.RxLoadError.JS_CACHE_INVALID.type);
                    return;
                }
                byte[] result = wrapperErrorResponse(e);
                if (Cenarius.DEBUG) {
                    LogUtil.i( "Exception: " + new String(result));
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
