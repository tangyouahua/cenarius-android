package org.apache.cordova.cordovautil.XWalkCordova;

import org.apache.cordova.cordovautil.WebViewClientForActivity;

import org.crosswalk.engine.XWalkWebViewEngine;

/**
 * ********************************************
 *
 * @Description 针对于Activity类型的Cordova（XWalk内核的）
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 11:12
 * *********************************************
 */
public class XWalkActivityWebViewClientCordova extends XWalkBaseWebViewClient {
    public XWalkActivityWebViewClientCordova(XWalkWebViewEngine parentEngine) {
        super(parentEngine);
        setWebViewClientListener(new WebViewClientForActivity());
    }
}
