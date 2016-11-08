package org.apache.cordova.cordovautil.XWalkCordova;

import org.apache.cordova.cordovautil.WebViewClientForFragment;

import org.crosswalk.engine.XWalkWebViewEngine;

/**
 * ********************************************
 *
 * @Description 针对于Fragment类型的Cordova（XWalk内核的）
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 11:12
 * *********************************************
 */
public class XWalkFragmentWebViewClientCordova extends XWalkBaseWebViewClient {
    public XWalkFragmentWebViewClientCordova(XWalkWebViewEngine parentEngine) {
        super(parentEngine);
        setWebViewClientListener(new WebViewClientForFragment());
    }
}
