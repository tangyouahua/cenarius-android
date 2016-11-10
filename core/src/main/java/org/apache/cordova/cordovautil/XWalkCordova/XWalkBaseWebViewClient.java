package org.apache.cordova.cordovautil.XWalkCordova;

import android.net.http.SslError;
import android.webkit.ValueCallback;

import com.m.cenarius.view.CenariusXWalkCordovaResourceClient;

import org.apache.cordova.cordovautil.WebViewClientListener;
import org.crosswalk.engine.XWalkWebViewEngine;
import org.xutils.common.util.LogUtil;
import org.xwalk.core.XWalkView;

/**
 * ********************************************
 *
 * @Description 网络的拦截处理器（XWalk内核的）
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 10:37
 * *********************************************
 */
public class XWalkBaseWebViewClient extends CenariusXWalkCordovaResourceClient {

    WebViewClientListener listener;

    public XWalkBaseWebViewClient(XWalkWebViewEngine parentEngine) {
        super(parentEngine);
    }

    public void setWebViewClientListener(WebViewClientListener listener) {
        this.listener = listener;
    }

    @Override
    public void onLoadStarted(XWalkView view, String url) {
        super.onLoadStarted(view, url);
        LogUtil.v("此时的onPageStarted: " + url);
        if (listener != null) {
            listener.onPageStarted(view, url, null);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
        LogUtil.v("此时的shouldOverrideUrlLoading: " + url);
        //此处可做拦截处理

        int isReturn = 0;// 0代表返回默认的super方法,  1代表返回true ,2代表返回false
        if (listener != null) {
            isReturn = listener.shouldOverrideUrlLoading(view, url);
        }
        if (isReturn == 1) {
            return true;
        } else if (isReturn == 2) {
            return false;
        } else {
            return super.shouldOverrideUrlLoading(view, url);
        }

    }


    @Override
    public void onLoadFinished(XWalkView view, String url) {
        super.onLoadFinished(view, url);
        LogUtil.v("此时的onPageFinished: " + url);
        if (listener != null) {
            listener.onPageFinished(view, url);
        }
    }


    @Override
    public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
        super.onReceivedLoadError(view, errorCode, description, failingUrl);
        LogUtil.v("此时的onReceivedError:  errorCode--> "
                + errorCode + "  description--> " + description + "  failingUrl--> " + failingUrl);
        //此处可做网络异常的处理
        if (listener != null) {
            listener.onReceivedError(view, errorCode, description, failingUrl);
        }
    }


    @Override
    public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
        super.onReceivedSslError(view, callback, error);
        LogUtil.v("此时的shouldOverrideUrlLoading: " + error != null ? error.getUrl() : " 链接为空");
//        if (BuildConfig.DEBUG){
//            callback.onReceiveValue(true);// debug = true
//        }else{
//            callback.onReceiveValue(false);// debug = false
//        }
    }



}
