package org.apache.cordova.cordovautil.SystemCordova;

import org.apache.cordova.cordovautil.WebViewClientForActivity;

import org.apache.cordova.engine.SystemWebViewEngine;

/**
 * ********************************************
 *
 * @Description 针对于Activity类型的Cordova
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 11:12
 * *********************************************
 */
public class CordovaActivityWebViewClientCordova extends CordovaBaseWebViewClient {
    public CordovaActivityWebViewClientCordova(SystemWebViewEngine parentEngine) {
        super(parentEngine);
        setWebViewClientListener(new WebViewClientForActivity());
    }
}
