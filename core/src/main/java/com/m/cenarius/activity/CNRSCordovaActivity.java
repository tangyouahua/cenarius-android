package com.m.cenarius.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.m.cenarius.utils.LogUtils;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

public class CNRSCordovaActivity extends CordovaActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();

        LogUtils.i(TAG, "loadUri , uri = " + (null != uri ? uri : "null"));
        if (TextUtils.isEmpty(uri)) {
            throw new IllegalArgumentException("[CenariusWebView] [loadUri] uri can not be null");
        }

//        Route route = RouteManager.getInstance().findRoute(uri);
//        if (null == route) {
//            LogUtils.i(TAG, "route not found");
//            return;
//        }
//
//        // 加载url
//        String remoteHTML = route.getHtmlFile();

        //
        String htmlUrl = htmlURL();
        loadUrl(htmlUrl);
    }

    private void setCorsswalk{
        View appCordovaView=appView.getView();//加载H5的View
        LogUtils.i(TAG,"此手机系统用到的内核为-->"+appCordovaView.getClass().getSimpleName());
        if (appCordovaView instanceof SystemWebView) {
            SystemWebViewEngine engine = (SystemWebViewEngine)appView.getEngine();
//            holder.engine=engine;
//            SystemWebView webView = (SystemWebView)engine.getView();
//            webView.setWebViewClient(new CordovaActivityWebViewClientCordova(engine));
//            webView.setWebChromeClient(new CordovaChromeClient(engine));
        } else if (appCordovaView instanceof XWalkCordovaView) {
            XWalkWebViewEngine engine = (XWalkWebViewEngine)appView.getEngine();
            holder.engine=engine;
            XWalkCordovaView webView = (XWalkCordovaView)engine.getView();
            webView.setResourceClient(new XWalkActivityWebViewClientCordova(engine));
            webView.setUIClient(new XWalkChromeClient(engine));
        }else{
            LogUtil.e("系统内核出故障，请检查...");
        }
    }

}
