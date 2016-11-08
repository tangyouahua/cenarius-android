package org.apache.cordova.cordovautil.SystemCordova;

import org.apache.cordova.cordovautil.WebViewClientForFragment;

import org.apache.cordova.engine.SystemWebViewEngine;

/**
 * ********************************************
 *
 * @Description 针对于Fragment类型的Cordova
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 11:12
 * *********************************************
 */
public class CordovaFragmentWebViewClientCordova extends CordovaBaseWebViewClient {
    public CordovaFragmentWebViewClientCordova(SystemWebViewEngine parentEngine) {
        super(parentEngine);
        setWebViewClientListener(new WebViewClientForFragment());
    }

}
