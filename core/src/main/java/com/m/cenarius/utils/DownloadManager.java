package com.m.cenarius.utils;

import com.m.cenarius.resourceproxy.cache.InternalCache;
import com.m.cenarius.route.Route;
import com.m.cenarius.view.CenariusHandleRequest;

import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.PipedOutputStream;

import static com.m.cenarius.view.CenariusHandleRequest.writeOutputStream;

/**
 * 下载线程，需要自己实现单例
 */

public class DownloadManager {

    public synchronized void startDownloadH5AndJs(final Route route, final PipedOutputStream outputStream) {
        RequestParams params = new RequestParams(route.getHtmlFile());
        params.setExecutor(new PriorityExecutor(1, true));

        x.http().get(params, new Callback.CommonCallback<byte[]>() {

            @Override
            public void onSuccess(byte[] result) {
                CenariusHandleRequest.writeOutputStream(outputStream, result);
                InternalCache.getInstance().saveCache(route, result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                byte[] result = CenariusHandleRequest.wrapperErrorThrowable(ex);
                writeOutputStream(outputStream, result);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }
}
