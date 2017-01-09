package com.m.cenarius.utils;

import com.m.cenarius.resourceproxy.cache.InternalCache;
import com.m.cenarius.route.Route;
import com.m.cenarius.view.CenariusHandleRequest;

import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static com.m.cenarius.view.CenariusHandleRequest.writeOutputStream;

/**
 * 下载线程，需要自己实现单例
 */

public class DownloadManager {

    private static volatile DownloadManager instance;
    private final static int MAX_DOWNLOAD_THREAD = 1; // 有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载.
    private final Executor executor = new PriorityExecutor(MAX_DOWNLOAD_THREAD, true);
    private final List<String> downloadInfoList = new ArrayList<>();

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

    public String getDownloadInfo(int index) {
        return downloadInfoList.get(index);
    }

    public synchronized void startDownload(String url, String savePath) {

    }

}
