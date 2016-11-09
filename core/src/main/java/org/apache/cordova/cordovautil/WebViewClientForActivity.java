package org.apache.cordova.cordovautil;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.m.cenarius.view.CenariusWidget;

import org.apache.cordova.engine.SystemWebView;

import java.util.ArrayList;
import java.util.List;

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



    private List<CenariusWidget> mWidgets = new ArrayList<>();

    /**
     * 自定义url拦截处理
     *
     * @param widget
     */
    public void addCenariusWidget(CenariusWidget widget) {
        if (null != widget) {
            mWidgets.add(widget);
        }
    }
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

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return null;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return null;
    }
}
