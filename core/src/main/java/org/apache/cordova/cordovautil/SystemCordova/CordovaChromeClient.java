package org.apache.cordova.cordovautil.SystemCordova;

import android.webkit.JsResult;
import android.webkit.WebView;

import org.apache.cordova.cordovautil.ChromeClient;
import org.apache.cordova.cordovautil.ChromeClientListener;

import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.xutils.common.util.LogUtil;

/**
 * ********************************************
 *
 * @Description 针对于系统内核ChromeClient
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 11:30
 * *********************************************
 */
public class CordovaChromeClient extends SystemWebChromeClient {
    ChromeClientListener listener;
    public CordovaChromeClient(SystemWebViewEngine parentEngine) {
        super(parentEngine);
        listener=new ChromeClient();
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        LogUtil.v("此时的onProgressChanged:   newProgress: "+newProgress);
        listener.onProgressChanged(view,newProgress);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        // 0代表返回默认的super方法,  1代表返回true ,2代表返回false
        LogUtil.v("此时的onJsAlert:   url: "+url+"  message: "+message);
        int isReturn=0;
        if (listener!=null) {
            isReturn= listener.onJsAlert(view, url, message, result,null);
        }
        if (isReturn==1) {
            return true;
        }else if(isReturn==2){
            return false;
        }else{
            return super.onJsAlert(view, url, message, result);
        }
    }
}
