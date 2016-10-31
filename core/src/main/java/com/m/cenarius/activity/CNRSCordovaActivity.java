package com.m.cenarius.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.m.cenarius.route.Route;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.LogUtils;

import org.apache.cordova.CordovaActivity;

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
}
