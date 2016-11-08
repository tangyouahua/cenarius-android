package org.apache.cordova.cordovautil;

import android.view.View;
import android.webkit.JsResult;

import org.xwalk.core.XWalkJavascriptResult;

/**
 * ********************************************
 *
 * @Description 网页拦截的接口回调处理
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 17:05
 * *********************************************
 */
public interface ChromeClientListener {
    void onProgressChanged(View view, int newProgress);
    // 0代表返回默认的super方法,  1代表返回true ,2代表返回false

    /**
     * @param view
     * @param url
     * @param message
     * @param jsResult  系统webView调用,不需要用到时可设为null
     * @param xWalkJavascriptResult  xWalk内核webView调用，不需要用到时可设null
     * @return
     */
    int onJsAlert(View view, String url, String message, JsResult jsResult, XWalkJavascriptResult xWalkJavascriptResult) ;
}
