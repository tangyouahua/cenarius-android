package org.apache.cordova.cordovautil.SystemCordova;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import org.apache.cordova.cordovautil.WebViewClientListener;

//import org.apache.cordova.BuildConfig;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.xutils.common.util.LogUtil;

/**
 * ********************************************
 *
 * @Description 网络的拦截处理器
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 10:37
 * *********************************************
 */
public class CordovaBaseWebViewClient extends SystemWebViewClient {

    WebViewClientListener listener;

    public CordovaBaseWebViewClient(SystemWebViewEngine parentEngine) {
        super(parentEngine);
    }

    public void setWebViewClientListener(WebViewClientListener listener) {
        this.listener=listener;
    }
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        LogUtil.v("此时的onPageStarted: "+url);
        if (listener!=null){
            listener.onPageStarted(view,url,favicon);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        LogUtil.v("此时的shouldOverrideUrlLoading: "+url);
        //此处可做拦截处理

        int isReturn=0;// 0代表返回默认的super方法,  1代表返回true ,2代表返回false
        if (listener!=null) {
            isReturn= listener.shouldOverrideUrlLoading(view, url);
        }
        if (isReturn==1) {
            return true;
        }else if(isReturn==2){
            return false;
        }else{
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        LogUtil.v("此时的onPageFinished: "+url);
        if (listener!=null){
            listener.onPageFinished(view,url);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        LogUtil.v("此时的onReceivedError:  errorCode--> "
                +errorCode+"  description--> "+description+"  failingUrl--> "+failingUrl);
        //此处可做网络异常的处理
        if (listener!=null){
            listener.onReceivedError(view,errorCode,description,failingUrl);
        }
    }
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//        if (BuildConfig.DEBUG){
//            handler.proceed();
//        }
    }

}
