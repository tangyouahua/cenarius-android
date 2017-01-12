package com.m.cenarius.view;

import android.os.Handler;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.resourceproxy.network.InterceptJavascriptInterface;
import com.m.cenarius.utils.Utils;

import org.crosswalk.engine.XWalkCordovaResourceClient;
import org.crosswalk.engine.XWalkWebViewEngine;
import org.xutils.common.util.LogUtil;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

public class CenariusXWalkCordovaResourceClient extends XWalkCordovaResourceClient {
    private  ProgressBar progressBar;
    private boolean isShowOver = false;
    public CenariusXWalkCordovaResourceClient(XWalkWebViewEngine parentEngine, ProgressBar progressBar) {
        super(parentEngine);
        this.progressBar = progressBar;
        mWebView = parentEngine.getView();
        mJSIntercept = new InterceptJavascriptInterface(this);
       if (mWebView instanceof XWalkView) {
            ((XWalkView) mWebView).addJavascriptInterface(mJSIntercept, "cenariusInterception");
        }
        isShowOver = false;
        progress = 50;
        handler.removeCallbacksAndMessages(null);
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

    // ajax 拦截
    private View mWebView = null;
    private InterceptJavascriptInterface mJSIntercept = null;
//    private InterceptJavascriptInterface.AjaxRequestContents mNextAjaxRequestContents = null;
//    private boolean isNextAjaxRequest = false;
//    private DownloadManager downloadManager = new DownloadManager();

//    public void nextMessageIsAjaxRequest(InterceptJavascriptInterface.AjaxRequestContents ajaxRequestContents) {
//        mNextAjaxRequestContents = ajaxRequestContents;
//        isNextAjaxRequest = true;
//    }

    int progress = 0;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            progress+=5;
            LogUtil.v("后半部分进度条增加值：" +progress);
            if (progress > 100) {
                progressBar.setProgress(100);
                handler.removeCallbacks(runnable);
                progressBar.setVisibility(View.GONE);//加载完网页进度条消失
            }else{
                progressBar.setProgress(progress);
                handler.postDelayed(this,50);
            }
        }
    };

    @Override
    public void onLoadStarted(XWalkView view, String url) {
        super.onLoadStarted(view, url);
        LogUtil.v("进度条加载： onLoadStarted");
    }

    @Override
    public void onProgressChanged(XWalkView view, int progressInPercent) {
        super.onProgressChanged(view,progressInPercent);
        if(progressBar == null || (isShowOver == true && progressInPercent >= 100)){
            return;
        }

        LogUtil.v("进度条加载： "+ progressInPercent);
        if (progressInPercent == 100) {
            isShowOver = true;
            LogUtil.v("进度条加载到100,开始定时加载后面一部分的进度");
            handler.post(runnable);
        } else {
            isShowOver =false;
            //先加载一半值，四舍五入取值，先取
            int progress = new BigDecimal(progressInPercent*0.5).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
            LogUtil.v("前半部分进度条增加值：" +progress);
            progressBar.setProgress(progress);//设置进度值
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
//        mNextAjaxRequestContents = null;
        mWidgets = getCenariusWidgets(view);
        if (CenariusHandleRequest.handleWidgets(view, url, mWidgets)) {
            return true;
        }

        return super.shouldOverrideUrlLoading(view, url);
    }

//    @Override
//    public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
//        if (Utils.hasLollipop()) {
//            WebResourceResponse webResourceResponse = handleResourceRequest(view, request.getUrl().toString());
//            //创建新的 XWalkWebResourceResponse
//            if (webResourceResponse != null) {
//                String mimeType = webResourceResponse.getMimeType();
//                String encoding = webResourceResponse.getEncoding();
//                int statusCode = webResourceResponse.getStatusCode();
//                String reasonPhrase = webResourceResponse.getReasonPhrase();
//                Map<String, String> headers = webResourceResponse.getResponseHeaders();
//                InputStream data = webResourceResponse.getData();
//                return createXWalkWebResourceResponse(mimeType, encoding, data, statusCode, reasonPhrase, headers);
//            }
//        }
//        return super.shouldInterceptLoadRequest(view, request);
//    }
//
//    @Override
//    public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
//        return handleResourceRequest(view, url);
//    }

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
        WebResourceResponse webResourceResponse;

//        if (isNextAjaxRequest) {
//            // ajax 请求
//            isNextAjaxRequest = false;
//            webResourceResponse = CenariusHandleRequest.handleAjaxRequest(requestUrl, mNextAjaxRequestContents.method, JSON.parseObject(mNextAjaxRequestContents.header, Map.class), mNextAjaxRequestContents.body);
//        } else {
//            // h5 请求
//            webResourceResponse = CenariusHandleRequest.handleResourceRequest(requestUrl);
//        }

        // h5 请求
        webResourceResponse = CenariusHandleRequest.handleResourceRequest(requestUrl);
        if (webResourceResponse != null) {
            return webResourceResponse;
        }
        return super.shouldInterceptLoadRequest(webView, requestUrl);
    }

}
