package com.m.cenarius.activity;

import android.os.Bundle;
import android.view.View;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.cordovautil.SystemCordova.CordovaActivityWebViewClientCordova;
import org.apache.cordova.cordovautil.SystemCordova.CordovaChromeClient;
import org.apache.cordova.cordovautil.XWalkCordova.XWalkActivityWebViewClientCordova;
import org.apache.cordova.cordovautil.XWalkCordova.XWalkChromeClient;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.crosswalk.engine.XWalkCordovaView;
import org.crosswalk.engine.XWalkWebViewEngine;
import org.xutils.common.util.LogUtil;

public class CNRSCordovaActivity extends CordovaActivity {

    /**
     * weViewEngine,两个内核都用这个接口
     */
    public CordovaWebViewEngine engine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();
        setCorsswalk();

        LogUtil.v("loadUri , uri = " + (null != uri ? uri : "null"));

        String htmlUrl = htmlURL();
        loadUrl(htmlUrl);
    }

    private void setCorsswalk() {
        View appCordovaView = appView.getView();//加载H5的View
        LogUtil.v("此手机系统用到的内核为-->" + appCordovaView.getClass().getSimpleName());
        if (appCordovaView instanceof SystemWebView) {
            SystemWebViewEngine engine = (SystemWebViewEngine) appView.getEngine();
            this.engine = engine;
            SystemWebView webView = (SystemWebView) engine.getView();
            webView.setWebViewClient(new CordovaActivityWebViewClientCordova(engine));
            webView.setWebChromeClient(new CordovaChromeClient(engine));
        } else if (appCordovaView instanceof XWalkCordovaView) {
            XWalkWebViewEngine engine = (XWalkWebViewEngine) appView.getEngine();
            this.engine = engine;
            XWalkCordovaView webView = (XWalkCordovaView) engine.getView();
            webView.setResourceClient(new XWalkActivityWebViewClientCordova(engine));
            webView.setUIClient(new XWalkChromeClient(engine));
        } else {
            LogUtil.e("系统内核出故障，请检查...");
        }
    }

}
