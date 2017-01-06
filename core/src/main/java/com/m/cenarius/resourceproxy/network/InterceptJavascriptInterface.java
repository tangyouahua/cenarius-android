package com.m.cenarius.resourceproxy.network;

import com.alibaba.fastjson.JSON;
import com.m.cenarius.utils.OpenApi;
import com.m.cenarius.utils.QueryUtil;
import com.m.cenarius.view.CenariusWebViewClient;
import com.m.cenarius.view.CenariusXWalkCordovaResourceClient;

import org.xutils.http.RequestParams;

import java.util.List;
import java.util.Map;

/**
 * 拦截 ajax
 */

public class InterceptJavascriptInterface {
    public static final String TAG = "InterceptJavascriptInterface";

    private Object mWebViewClient = null;

    public InterceptJavascriptInterface(Object webViewClient) {
        mWebViewClient = webViewClient;
    }

//    public class FormRequestContents {
//        public String method = null;
//        public String json = null;
//        public String enctype = null;
//
//        public FormRequestContents(String method, String json, String enctype) {
//            this.method = method;
//            this.json = json;
//            this.enctype = enctype;
//        }
//    }

//    public class AjaxRequestContents {
//        public String method = null;
//        public String header = null;
//        public String body = null;
//
//        public AjaxRequestContents(String method, String header, String body) {
//            this.method = method;
//            this.header = header;
//            this.body = body;
//        }
//    }

//    @org.xwalk.core.JavascriptInterface
//    @android.webkit.JavascriptInterface
//    public void customAjax(final String method, final String header, final String body) {
//        if (mWebViewClient instanceof CenariusXWalkCordovaResourceClient) {
//            ((CenariusXWalkCordovaResourceClient) mWebViewClient).nextMessageIsAjaxRequest(new AjaxRequestContents(method, header, body));
//        } else if (mWebViewClient instanceof CenariusWebViewClient) {
//            ((CenariusWebViewClient) mWebViewClient).nextMessageIsAjaxRequest(new AjaxRequestContents(method, header, body));
//        }
//    }

    @org.xwalk.core.JavascriptInterface
    @android.webkit.JavascriptInterface
    public String getUrlSign(final String url, final String headerString, final String bodyString) {
        return OpenApi.openApiForAjax(url, headerString, bodyString);
    }

//    @JavascriptInterface
//    public void customSubmit(String json, String method, String enctype) {
//        Log.i(TAG, "Submit data: " + json + "\t" + method + "\t" + enctype);
//        mWebViewClient.nextMessageIsFormRequest(
//                new FormRequestContents(method, json, enctype));
//    }
}
