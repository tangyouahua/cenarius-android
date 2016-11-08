package org.apache.cordova.cordovautil;

import android.graphics.Bitmap;
import android.view.View;

import org.apache.cordova.engine.SystemWebView;

/**
 * ********************************************
 *
 * @Description SystemCordova与XwalkCordova的内核拦截WebViewClien,对于Activity所做出的拦截
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 17:31
 * *********************************************
 */
public class WebViewClientForActivity implements WebViewClientListener {
    @Override
    public void onPageFinished(View view, String url) {
        if (view instanceof SystemWebView){

        }else{//View 等于XWalkCordovaView

        }
    }

    @Override
    public int shouldOverrideUrlLoading(View view, String url) {
//        if (!TextUtils.isEmpty(url)&&(url.contains("http://")||url.contains("https://"))){
//            if (view instanceof WebView){
//                ((SystemWebView)view).loadUrl(url);
//            }else{//View 等于XWalkCordovaView
//                ((XWalkView)view).load(url,null);
//            }
//            return 1;
//        }
        return 0;
    }

    @Override
    public void onPageStarted(View view, String url, Bitmap favicon) {

    }

    @Override
    public void onReceivedError(View view, int errorCode, String description, String failingUrl) {

    }
}
