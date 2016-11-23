package com.m.cenarius.view;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;

import com.m.cenarius.Cenarius;
import com.m.cenarius.Constants;
import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.resourceproxy.ResourceProxy;
import com.m.cenarius.resourceproxy.cache.AssetCache;
import com.m.cenarius.resourceproxy.cache.CacheEntry;
import com.m.cenarius.resourceproxy.cache.CacheHelper;
import com.m.cenarius.route.Route;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.Utils;
import com.m.cenarius.utils.io.IOUtils;

import org.apache.http.conn.ConnectTimeoutException;
import org.crosswalk.engine.XWalkCordovaResourceClient;
import org.crosswalk.engine.XWalkWebViewEngine;
import org.json.JSONObject;
import org.xutils.common.util.LogUtil;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.GzipSource;

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


//    /**
//     * 拦截资源请求，部分资源需要返回本地资源
//     * <p>
//     * <p>
//     * html，js资源直接渲染进程返回,图片等其他资源先返回空的数据流再异步向流中写数据
//     * <p>
//     * <p>
//     * <note>这个方法会在渲染线程执行，如果做了耗时操作会block渲染</note>
//     */
//    private XWalkWebResourceResponse handleResourceRequest(XWalkView webView,XWalkWebResourceRequest request) {
//        LogUtil.i( "[handleResourceRequest] url =  " + request.getUrl().toString());
//        String uriString = uriForUrl(request.getUrl().toString());
//        Uri finalUri = Uri.parse(uriString);
//        String baseUri = finalUri.getPath();
//        Route route = RouteManager.getInstance().findRoute(baseUri);
//        if (route == null) {
//            return super.shouldInterceptLoadRequest(webView, request);
//        }
//
//        String htmlFileURL = CacheHelper.getInstance().localHtmlURLForURI(uriString);
//        if (htmlFileURL == null) {
//            htmlFileURL = CacheHelper.getInstance().remoteHtmlURLForURI(uriString);
//        }
//
//
//
//        return super.shouldInterceptLoadRequest(webView, htmlFileURL);
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
        String fileUrl = CenariusHandleRequest.fileUrlFor(requestUrl);
        if (fileUrl != null) {
            requestUrl = fileUrl;
        }

        return super.shouldInterceptLoadRequest(webView, requestUrl);
    }

    // html js 直接返回
//        if (Helper.isHtmlResource(requestUrl) || Helper.isJsResource(requestUrl)) {
//            String htmlFileURL = CacheHelper.getInstance().localHtmlURLForURI(uriString);
//            if (htmlFileURL == null) {
//                htmlFileURL = CacheHelper.getInstance().remoteHtmlURLForURI(uriString);
//            }
//            return super.shouldInterceptRequest(webView, htmlFileURL);
//
//
//            final CacheEntry cacheEntry = CacheHelper.getInstance().findCache(route);
//            if (null == cacheEntry) {
//                // 没有cache，
//                return super.shouldInterceptRequest(webView, requestUrl);
//            }
//            if (!cacheEntry.isValid()) {
//                // 有cache但无效，清除缓存
//                CacheHelper.getInstance().removeCache(route);
//                return super.shouldInterceptRequest(webView, requestUrl);
//            } else {
//                //读缓存
//                LogUtil.i( "cache hit :" + requestUrl);
//                String data = "";
//                try {
//                    data = IOUtils.toString(cacheEntry.inputStream);
//                    // hack 检查cache是否完整
//                    if (TextUtils.isEmpty(data)) {
//                        CacheHelper.getInstance().removeCache(route);
//                        return super.shouldInterceptRequest(webView, requestUrl);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    // hack 检查cache是否完整
//                    CacheHelper.getInstance().removeCache(route);
//                    return super.shouldInterceptRequest(webView, requestUrl);
//                }
//                return new WebResourceResponse(Constants.MIME_TYPE_HTML, "utf-8", IOUtils.toInputStream(data));
//            }
//        }
//
//        // js直接返回
//        if (Helper.isJsResource(requestUrl)) {
//            final CacheEntry cacheEntry = CacheHelper.getInstance().findCache(route);
//            if (null == cacheEntry) {
//                // 后面逻辑会通过network去加载
//                // 加载后再显示
//            } else if (!cacheEntry.isValid()) {
//                // 后面逻辑会通过network去加载
//                // 加载后再显示
//                // 清除缓存
//                CacheHelper.getInstance().removeCache(route);
//            } else {
//                String data = "";
//                try {
//                    data = IOUtils.toString(cacheEntry.inputStream);
//                    if (TextUtils.isEmpty(data) || (cacheEntry.length > 0 && cacheEntry.length != data.length())) {
//                        CacheHelper.getInstance().removeCache(route);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    CacheHelper.getInstance().removeCache(route);
//                }
//                LogUtil.i( "cache hit :" + requestUrl);
//                return new WebResourceResponse(Constants.MIME_TYPE_HTML, "utf-8", IOUtils.toInputStream(data));
//            }
//        }
//
//        // 图片等其他资源使用先返回空流，异步写数据
//        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(requestUrl);
//        String mimeType = MimeUtils.guessMimeTypeFromExtension(fileExtension);
//        try {
//            LogUtil.i( "start load async :" + requestUrl);
//            final PipedOutputStream out = new PipedOutputStream();
//            final PipedInputStream in = new PipedInputStream(out);
//            WebResourceResponse xResponse = new WebResourceResponse(mimeType, "UTF-8", in);
//            if (Utils.hasLollipop()) {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Access-Control-Allow-Origin", "*");
//                xResponse.setResponseHeaders(headers);
//            }
//            final String url = requestUrl;
//            webView.post(new Runnable() {
//                @Override
//                public void run() {
//                    new Thread(new ResourceRequest(url, out, in)).start();
//                }
//            });
//            return xResponse;
//        } catch (IOException e) {
//            e.printStackTrace();
//            LogUtils.e(TAG, "url : " + requestUrl + " " + e.getMessage());
//            return super.shouldInterceptRequest(webView, requestUrl);
//        } catch (Throwable e) {
//            e.printStackTrace();
//            LogUtils.e(TAG, "url : " + requestUrl + " " + e.getMessage());
//            return super.shouldInterceptRequest(webView, requestUrl);
//        }
//    }

}
