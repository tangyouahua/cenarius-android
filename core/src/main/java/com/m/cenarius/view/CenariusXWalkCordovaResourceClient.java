package com.m.cenarius.view;

import android.view.View;
import android.webkit.WebResourceResponse;

import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.utils.Utils;

import org.crosswalk.engine.XWalkCordovaResourceClient;
import org.crosswalk.engine.XWalkWebViewEngine;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class CenariusXWalkCordovaResourceClient extends XWalkCordovaResourceClient {

    public CenariusXWalkCordovaResourceClient(XWalkWebViewEngine parentEngine) {
        super(parentEngine);
    }

    private ArrayList<CenariusWidget> mWidgets;


    public ArrayList<CenariusWidget> getCenariusWidgets(View view) {
        if (mWidgets == null) {
            if (null != view && view.getContext() instanceof CNRSViewActivity) {
                mWidgets = ((CNRSViewActivity) view.getContext()).widgets;
            }
        }
        return mWidgets;
    }

    @Override
    public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
        if (CenariusHandleRequest.handleWidgets(view, url, mWidgets)) {
            return true;
        }

        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
        if (Utils.hasLollipop()) {
            WebResourceResponse webResourceResponse = handleResourceRequest(view, request.getUrl().toString());
            //创建新的 XWalkWebResourceResponse
            String mimeType = webResourceResponse.getMimeType();
            String encoding = webResourceResponse.getEncoding();
            int statusCode = webResourceResponse.getStatusCode();
            String reasonPhrase = webResourceResponse.getReasonPhrase();
            Map<String, String> headers = webResourceResponse.getResponseHeaders();
            InputStream data = webResourceResponse.getData();
            return createXWalkWebResourceResponse(mimeType, encoding, data, statusCode, reasonPhrase, headers);
        } else {
            return super.shouldInterceptLoadRequest(view, request);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
        return handleResourceRequest(view, url);
    }

    /**
     * 拦截资源请求，部分资源需要返回本地资源
     * <p>
     * <p>
     * html，js资源直接渲染进程返回,图片等其他资源先返回空的数据流再异步向流中写数据
     * <p>
     * <p>
     * <note>这个方法会在渲染线程执行，如果做了耗时操作会block渲染</note>
     */
    private WebResourceResponse handleResourceRequest(XWalkView webView, String requestUrl) {
        WebResourceResponse webResourceResponse = CenariusHandleRequest.handleResourceRequest(webView, requestUrl);
        if (webResourceResponse != null)
        {
            return webResourceResponse;
        }

        return super.shouldInterceptLoadRequest(webView, requestUrl);
    }

}
