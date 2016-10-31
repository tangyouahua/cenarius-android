package com.m.cenarius.resourceproxy.network;

import com.m.cenarius.Constants;
import com.m.cenarius.Cenarius;
import com.m.cenarius.resourceproxy.cache.CacheEntry;
import com.m.cenarius.resourceproxy.cache.CacheHelper;
import com.m.cenarius.route.Route;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.utils.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


public class HtmlHelper {

    public static final String TAG = HtmlHelper.class.getSimpleName();
    public static final List<String> mDownloadingProcess = new ArrayList<>();

    /**
     * 下载html文件
     *
     * @param url
     * @param callback
     */
    private static void doDownloadHtmlFile(String url, Callback callback) {
        LogUtils.i(TAG, "url = " + url);
        Request request = new Request.Builder().url(url)
                .build();
        Cenarius.getOkHttpClient().newCall(request)
                .enqueue(callback);
    }

    /**
     * 下载html文件，然后缓存
     *
     * @param route
     * @param callback
     */
    public static void prepareHtmlFile(final Route route, final Callback callback) {
        HtmlHelper.doDownloadHtmlFile(route.getHtmlFile(), new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        // 1. 存储到本地
                        boolean result = CacheHelper.getInstance().saveHtmlCache(route, IOUtils.toByteArray(response.body()
                                .byteStream()));
                        // 存储失败，则失败
                        if (!result) {
                            onFailure(call, new IOException("file save fail!"));
                            return;
                        }
                    }
                    // 2. 通知外面去查找
                    if (null != callback) {
                        callback.onResponse(call, response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(call, new IOException("file save fail!"));
                    LogUtils.i(TAG, "prepare html fail");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (null != callback) {
                    callback.onFailure(call, e);
                }
            }
        });
    }

    /**
     * 空闲时间下载html文件
     */
    public static void prepareHtmlFiles(ArrayList<Route> routes) {
        if (null == routes || routes.isEmpty()) {
            return;
        }
//        ArrayList<Route> validRoutes = new ArrayList<>();
//        validRoutes.addAll(routes.items);
//        validRoutes.addAll(routes.partialItems);
        // 重新下载
        mDownloadingProcess.clear();
        for (final Route route : routes) {
            CacheEntry htmlFile = CacheHelper.getInstance().findHtmlCache(route);
            if (null == htmlFile) {
                if (!mDownloadingProcess.contains(route.getHtmlFile())) {
                    mDownloadingProcess.add(route.getHtmlFile());
                    HtmlHelper.prepareHtmlFile(route, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // 如果下载失败，则不移除
//                            mDownloadingProcess.remove(route.getHtmlFile());
                            LogUtils.i(TAG, "download html failed" + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            mDownloadingProcess.remove(route.getHtmlFile());
                            LogUtils.i(TAG, "download html success");
                            // 如果全部文件下载成功，则发送校验成功事件
                            if (mDownloadingProcess.isEmpty()) {
                                LogUtils.i(TAG, "download html complete");
                                BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_ROUTE_CHECK_VALID, null));
                            }
                        }
                    });
                }
            } else {
                htmlFile.close();
            }
        }
    }
}
