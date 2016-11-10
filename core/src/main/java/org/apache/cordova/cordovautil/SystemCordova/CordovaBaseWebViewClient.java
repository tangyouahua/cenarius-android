package org.apache.cordova.cordovautil.SystemCordova;

import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
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
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.utils.Utils;
import com.m.cenarius.utils.io.IOUtils;
import com.m.cenarius.view.CenariusWebView;
import com.m.cenarius.view.CenariusWebViewClient;
import com.m.cenarius.view.CenariusWebViewCore;
import com.m.cenarius.view.CenariusWidget;

import org.apache.cordova.cordovautil.WebViewClientListener;

//import org.apache.cordova.BuildConfig;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;
import org.xutils.common.util.LogUtil;

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

/**
 * ********************************************
 *
 * @Description 网络的拦截处理器
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 10:37
 * *********************************************
 */
public class CordovaBaseWebViewClient extends SystemWebViewClient {

    WebViewClientListener listener;

    public CordovaBaseWebViewClient(SystemWebViewEngine parentEngine) {
        super(parentEngine);
    }

    public void setWebViewClientListener(WebViewClientListener listener) {
        this.listener=listener;
    }
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        LogUtil.v("此时的onPageStarted: "+url);
        if (listener!=null){
            listener.onPageStarted(view,url,favicon);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        LogUtil.v("此时的onPageFinished: "+url);
        if (listener!=null){
            listener.onPageFinished(view,url);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        LogUtil.v("此时的onReceivedError:  errorCode--> "
                +errorCode+"  description--> "+description+"  failingUrl--> "+failingUrl);
        //此处可做网络异常的处理
        if (listener!=null){
            listener.onReceivedError(view,errorCode,description,failingUrl);
        }
    }
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//        if (BuildConfig.DEBUG){
//            handler.proceed();
//        }
    }

}
