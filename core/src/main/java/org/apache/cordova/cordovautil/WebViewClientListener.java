package org.apache.cordova.cordovautil;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

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
public interface WebViewClientListener {
    void onPageFinished(View arg0, String arg1);
    // 0代表返回默认的super方法,  1代表返回true ,2代表返回false
    int shouldOverrideUrlLoading(View view, String url);
    void onPageStarted(View view, String url, Bitmap favicon);
    void onReceivedError(View view, int errorCode, String description, String failingUrl);

}
