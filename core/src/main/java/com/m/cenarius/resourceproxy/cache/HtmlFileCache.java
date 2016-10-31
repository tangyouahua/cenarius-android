package com.m.cenarius.resourceproxy.cache;

import android.content.Context;

import com.m.cenarius.Constants;
import com.m.cenarius.route.Route;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.utils.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * 缓存html文件
 *
 * html作为页面入口，在缓存上单独处理。
 *
 * 存储位置默认在/data/data/html下
 *
 */
public class HtmlFileCache implements ICache {

    public static final String TAG = "HtmlFileCache";

    public HtmlFileCache() {
    }

    @Override
    public CacheEntry findCache(Route route) {
        File file = file(route);
        if (file.exists() && file.canRead()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bytes = IOUtils.toByteArray(fileInputStream);
                CacheEntry cacheEntry = new CacheEntry(file.length(), new ByteArrayInputStream(bytes));
                fileInputStream.close();
                LogUtils.i(TAG, "hit");
                return cacheEntry;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean removeCache(Route route) {
        LogUtils.i(TAG, "remove cache  : url " + route.uri);
        File file = file(route);
        return file.exists() && file.delete();
    }

    /**
     * 保存文件缓存
     *
     * @param route         html的route
     * @param bytes html数据
     */
    public boolean saveCache(Route route, byte[] bytes) {
        if (null == bytes || bytes.length == 0) {
            return false;
        }
        File fileDir = fileDir();
        if (!fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                return false;
            }
        }
        // 如果存在，则先删掉之前的缓存
        removeCache(route);
        File saveFile = null;
        try {
            saveFile = file(route);
            OutputStream outputStream = new FileOutputStream(saveFile);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (null != saveFile && saveFile.exists()) {
                saveFile.exists();
            }
        }
        return false;
    }

    /**
     * 清除html缓存
     *
     * @return whether clear cache successfully
     */
    public boolean clear() {
        File htmlDir = fileDir();
        if (!htmlDir.exists()) {
            return true;
        }
        File[] htmlFiles = htmlDir.listFiles();
        if (null == htmlFiles) {
            return true;
        }
        boolean processed = true;
        for (File file : htmlFiles) {
            if (!file.delete()) {
                processed = false;
            }
        }
        return processed;
    }

    /**
     * html存储目录
     *
     * @return html存储目录
     */
    private File fileDir() {
        return new File(AppContext.getInstance().getDir(Constants.CACHE_HOME_DIR,
                Context.MODE_PRIVATE), Constants.DEFAULT_DISK_HTML_FILE_PATH);
    }

    /**
     * 单个html存储文件路径
     *
     * @param route route
     * @return html对应的存储文件
     */
    private File file(Route route) {
        String fileName = route.fileHash + Constants.EXTENSION_HTML;
        return new File(fileDir(), fileName);
    }
}