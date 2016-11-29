package com.m.cenarius.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.m.cenarius.Constants;
import com.m.cenarius.Cenarius;
import com.m.cenarius.route.Route;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.utils.Utils;

/**
 * route doLoadCache
 * 设置client
 * 协议
 */
public class CenariusWebViewCore extends SafeWebView {

    public static final String TAG = CenariusWebViewCore.class.getSimpleName();

    /**
     *
     */
    public interface UriLoadCallback {

        /**
         * 开始load uri
         */
        boolean onStartLoad();

        /**
         * 开始下载html
         */
        boolean onStartDownloadHtml();

        /**
         * load成功
         */
        boolean onSuccess();

        /**
         * load失败
         * @param error
         */
        boolean onFail(RxLoadError error);
    }

    public static class SimpleUriLoadCallback implements UriLoadCallback {

        @Override
        public boolean onStartLoad() {
            return false;
        }

        @Override
        public boolean onStartDownloadHtml() {
            return false;
        }

        @Override
        public boolean onSuccess() {
            return false;
        }

        @Override
        public boolean onFail(RxLoadError error) {
            return false;
        }
    }

    /**
     * Cenarius doLoadCache error
     */
    public enum RxLoadError {
        ROUTE_NOT_FOUND(0, "无法找到合适的Route"), // route找不到
        HTML_NO_CACHE(1, "找不到html缓存"), // 没有缓存
        HTML_DOWNLOAD_FAIL(2, "资源加载失败"), // html下载失败
        HTML_CACHE_INVALID(3, "html缓存失效"), // html缓存实效
        JS_CACHE_INVALID(4, "js缓存失效"), // js
        UNKNOWN(10, "unknown");

        public String messsage;
        public int type;
        RxLoadError(int type, String message) {
            this.type = type;
            this.messsage = message;
        }

        public static RxLoadError parse(int type) {
            switch (type) {
                case 0: {
                    return ROUTE_NOT_FOUND;
                }
                case 1: {
                    return HTML_NO_CACHE;
                }
                case 2: {
                    return HTML_DOWNLOAD_FAIL;
                }
                case 3: {
                    return HTML_CACHE_INVALID;
                }
                case 4: {
                    return JS_CACHE_INVALID;
                }
                default: {
                    return UNKNOWN;
                }
            }
        }
    }

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private CenariusWebViewClient mWebViewClient;
    private CenariusWebChromeClient mWebChromeClient;

    public CenariusWebViewCore(Context context) {
        super(context);
        setup();
    }

    public CenariusWebViewCore(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CenariusWebViewCore(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        setBackgroundColor(Color.WHITE);
        WebSettings ws = getSettings();
        setupWebSettings(ws);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(Cenarius.DEBUG);
        }
        if (null == mWebViewClient) {
            mWebViewClient = new CenariusWebViewClient(this);
        }
        setWebViewClient(mWebViewClient);
        if (null == mWebChromeClient) {
            mWebChromeClient = new CenariusWebChromeClient();
        }
        setWebChromeClient(mWebChromeClient);
        setDownloadListener(getDownloadListener());
    }

    @TargetApi(16)
    @SuppressLint("SetJavaScriptEnabled")
    protected void setupWebSettings(WebSettings ws) {
        ws.setAppCacheEnabled(true);
        ws.setJavaScriptEnabled(true);
        ws.setGeolocationEnabled(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);

        ws.setAllowFileAccess(true);
        if (Utils.hasJellyBean()) {
            ws.setAllowFileAccessFromFileURLs(true);
            ws.setAllowUniversalAccessFromFileURLs(true);
        }

        // enable html cache
        ws.setDomStorageEnabled(true);
        ws.setAppCacheEnabled(true);
        // Set cache size to 8 mb by default. should be more than enough
        ws.setAppCacheMaxSize(1024 * 1024 * 8);
        // This next one is crazy. It's the DEFAULT location for your app's cache
        // But it didn't work for me without this line
        ws.setAppCachePath("/data/data/" + getContext().getPackageName() + "/cache");
        ws.setAllowFileAccess(true);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);

        String ua = ws.getUserAgentString() + " " + Cenarius.getUserAgent();
        ws.setUserAgentString(ua);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ws.setUseWideViewPort(true);
        }

        if (Utils.hasLollipop()) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    protected DownloadListener getDownloadListener() {
        return new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimeType, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(url);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 自定义url拦截处理
     *
     * @param widget
     */
    public void addCenariusWidget(CenariusWidget widget) {
        if (null == widget) {
            return;
        }
        mWebViewClient.addCenariusWidget(widget);
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        if (!(client instanceof CenariusWebViewClient)) {
            throw new IllegalArgumentException("client must inherit CenariusWebViewClient");
        }
        if (null != mWebViewClient) {
            for (CenariusWidget widget : mWebViewClient.getCenariusWidgets()) {
                if (null != widget) {
                    ((CenariusWebViewClient) client).addCenariusWidget(widget);
                }
            }
        }
        mWebViewClient = (CenariusWebViewClient) client;
        super.setWebViewClient(client);
    }

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        if (!(client instanceof CenariusWebChromeClient)) {
            throw new IllegalArgumentException("client must inherit CenariusWebViewClient");
        }
        mWebChromeClient = (CenariusWebChromeClient) client;
        super.setWebChromeClient(client);
    }

}
