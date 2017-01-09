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

        initProgressBar();
        setCrosswalk();
    }

    private void initProgressBar() {
        FrameLayout containerView = (FrameLayout) this.findViewById(android.R.id.content);
        ViewParent parent = containerView.getParent();
        pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.height = (int) getResources().getDimension(R.dimen.progress_bar_height);
        pb.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_bg));
        containerView.addView(pb, lp);
    }
}
