package com.m.cenarius.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.m.cenarius.R;
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

    public View progress;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();

        Log.v("cenarius", "loadUri , uri = " + (null != uri ? uri : "null"));

        String htmlUrl = htmlURL();
        if (htmlUrl != null) {
            loadUrl("https://wechat-dev.infinitus.com.cn/wechat-front/html5/build/magazineAddress/index.html?staffNum=220023706#modify");
        } else {
            Log.v("cenarius", "htmlUrl 为空");
        }
    }
}
