package com.m.cenarius.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.m.cenarius.view.CenariusXWalkCordovaResourceClient;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.crosswalk.engine.XWalkCordovaUiClient;
import org.crosswalk.engine.XWalkCordovaView;
import org.crosswalk.engine.XWalkWebViewEngine;

public class CNRSCordovaActivity extends CordovaActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();
        setCorsswalk();

        Log.v("cenarius", "loadUri , uri = " + (null != uri ? uri : "null"));

        String htmlUrl = htmlURL();
        if (htmlUrl != null) {
            loadUrl(htmlUrl);
        } else {
            Log.v("cenarius", "htmlUrl 为空");
        }
    }

    /**
     * 设置 webView 的 WebViewClient 和 WebChromeClient。
     * 如果要自定义它们，可以 Override
     */
    public void setCorsswalk() {
        View appCordovaView = appView.getView();//加载H5的View
        Log.v("cenarius", "此手机系统用到的内核为-->" + appCordovaView.getClass().getSimpleName());
        if (appCordovaView instanceof SystemWebView) {
            SystemWebViewEngine engine = (SystemWebViewEngine) appView.getEngine();
            SystemWebView webView = (SystemWebView) engine.getView();
            webView.setWebViewClient(new SystemWebViewClient(engine));
            webView.setWebChromeClient(new SystemWebChromeClient(engine));
        } else if (appCordovaView instanceof XWalkCordovaView) {
            XWalkWebViewEngine engine = (XWalkWebViewEngine) appView.getEngine();
            XWalkCordovaView webView = (XWalkCordovaView) engine.getView();
            webView.setResourceClient(new CenariusXWalkCordovaResourceClient(engine));
            webView.setUIClient(new XWalkCordovaUiClient(engine));
        } else {
            Log.e("cenarius", "系统内核出故障，请检查...");
        }
    }

}
