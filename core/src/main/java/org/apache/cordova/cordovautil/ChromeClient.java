package org.apache.cordova.cordovautil;

import android.view.View;
import android.webkit.JsResult;

import org.apache.cordova.engine.SystemWebView;
import org.xwalk.core.XWalkJavascriptResult;

/**
 * ********************************************
 *
 * @Description SystemCordova与XwalkCordova的ChromeClient拦截处理，Activity与Fragment共用
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 17:49
 * *********************************************
 */
public class ChromeClient implements ChromeClientListener {
    @Override
    public void onProgressChanged(View view, int newProgress) {
        if (view instanceof SystemWebView){

        }else{//View 等于XWalkCordovaView

        }
    }

    @Override
    public int onJsAlert(View view, String url, String message, JsResult jsResult, XWalkJavascriptResult xWalkJavascriptResult) {
        // 0代表返回默认的super方法,  1代表返回true ,2代表返回false
        return 0;
    }
}
