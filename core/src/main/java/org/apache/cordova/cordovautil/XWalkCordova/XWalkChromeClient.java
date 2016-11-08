package org.apache.cordova.cordovautil.XWalkCordova;

import org.apache.cordova.cordovautil.ChromeClient;
import org.apache.cordova.cordovautil.ChromeClientListener;

import org.crosswalk.engine.XWalkCordovaUiClient;
import org.crosswalk.engine.XWalkWebViewEngine;
import org.xutils.common.util.LogUtil;
import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkView;

/**
 * ********************************************
 *
 * @Description
 * @author:YuSy
 * @E-mail:you551@163.com
 * @qq:447234062
 * @date 2016/10/26 17:45
 * *********************************************
 */
public class XWalkChromeClient extends XWalkCordovaUiClient {
    ChromeClientListener listener;
    public XWalkChromeClient(XWalkWebViewEngine parentEngine) {
        super(parentEngine);
        listener=new ChromeClient();
    }

    @Override
    public void onScaleChanged(XWalkView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        LogUtil.v("此时的onScaleChanged:   oldScale: "+oldScale+"  newScale: "+newScale);
        if (listener!=null){
            listener.onProgressChanged(view,Float.valueOf(newScale).intValue());
        }
    }

    @Override
    public boolean onJsAlert(XWalkView view, String url, String message, XWalkJavascriptResult result) {
        LogUtil.v("此时的onJsAlert:   url: "+url+"  message: "+message);
        // 0代表返回默认的super方法,  1代表返回true ,2代表返回false
        int isReturn=0;
        if (listener!=null) {
            isReturn= listener.onJsAlert(view, url, message,null, result);
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
