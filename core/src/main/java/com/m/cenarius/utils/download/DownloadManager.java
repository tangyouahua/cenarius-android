package com.m.cenarius.utils.download;

import com.m.cenarius.resourceproxy.cache.InternalCache;
import com.m.cenarius.route.Route;

import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.common.util.LogUtil;
import org.xutils.db.converter.ColumnConverterFactory;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 下载管理类
 */
public final class DownloadManager {

    private static volatile DownloadManager instance;
    private final static int MAX_DOWNLOAD_THREAD = 1; // 有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载.
    private final Executor executor = new PriorityExecutor(MAX_DOWNLOAD_THREAD, true);
    private List<Route> downloadInfoList = new ArrayList<>();
    private final ConcurrentHashMap<DownloadInfo, DownloadCallback>
            callbackMap = new ConcurrentHashMap<DownloadInfo, DownloadCallback>(5);

    public static DownloadManager getInstance() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }

    public int getDownloadListCount() {
        return downloadInfoList.size();
    }

    public Route getDownloadInfo(int index) {
        return downloadInfoList.get(index);
    }

    public void addDownload(Route route) {
        downloadInfoList.remove(route);
        downloadInfoList.add(0, route);
        startDownload();
    }

    public synchronized void startDownload() {
        if (downloadInfoList.isEmpty()) {
            return;
        }
        Route route = downloadInfoList.get(0);
        RequestParams params = new RequestParams(route.getHtmlFile());
        params.setExecutor(executor);
        try {
            byte[] result = x.http().getSync(params, byte[].class);
            InternalCache.getInstance().saveCache(route, result);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void removeAllDownload() {
        downloadInfoList = new ArrayList<>();
    }

}
