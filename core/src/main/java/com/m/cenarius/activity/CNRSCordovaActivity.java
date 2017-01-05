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

//        setCrosswalk();

        Log.v("cenarius", "loadUri , uri = " + (null != uri ? uri : "null"));

        String htmlUrl = htmlURL();
        if (htmlUrl != null) {
            loadUrl(htmlUrl);
        } else {
            Log.v("cenarius", "htmlUrl 为空");
        }


    }



}
