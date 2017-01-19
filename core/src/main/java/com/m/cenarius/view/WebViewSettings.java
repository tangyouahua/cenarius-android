package com.m.cenarius.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.webkit.WebSettings;

import com.m.cenarius.Cenarius;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.Utils;

import org.xwalk.core.XWalkSettings;

/**
 * Created by m on 2017/1/19.
 */

public class WebViewSettings {

    @TargetApi(16)
    @SuppressLint("SetJavaScriptEnabled")
    public static void setupWebSettings(WebSettings ws) {
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
        ws.setAppCachePath("/data/data/" + AppContext.getInstance().getPackageName() + "/cache");
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

}
