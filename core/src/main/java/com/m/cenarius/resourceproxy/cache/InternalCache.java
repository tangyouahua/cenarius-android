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
 * 缓存资源文件
 * <p>
 * 存储位置默认在/data/data/www下
 */

public class InternalCache implements ICache {

    public static final String TAG = "InternalCache";

    public static InternalCache getInstance() {
        return new InternalCache();
    }

    @Override
    public CacheEntry findCache(Route route) {
        if (route == null) {
            return null;
        }
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

    /**
     * 删除单个资源缓存
     *
     * @param route 资源地址
     */
    @Override
    public boolean removeCache(Route route) {
        File file = file(route);
        return file.exists() && file.delete();
    }

    /**
     * 保存文件缓存
     *
     * @param route route
     * @param bytes 数据
     */
    public boolean saveCache(Route route, byte[] bytes) {
        if (null == bytes || bytes.length == 0) {
            return false;
        }
        // 如果存在，则先删掉之前的缓存
        removeCache(route);
        File saveFile = null;
        try {
            saveFile = file(route);
            File fileDir = saveFile.getParentFile();
            if (!fileDir.exists()) {
                if (!fileDir.mkdirs()) {
                    return false;
                }
            }
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
     * 清除缓存
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
     * 存储目录
     *
     * @return 存储目录
     */
    private File fileDir() {
        return new File(AppContext.getInstance().getDir(Constants.CACHE_HOME_DIR, Context.MODE_PRIVATE), Constants.DEFAULT_DISK_INTERNAL_FILE_PATH);
    }

    /**
     * 单个存储文件路径
     *
     * @param route route
     * @return html对应的存储文件
     */
    public File file(Route route) {
        return new File(fileDir(), route.uri);
    }

    /**
     * 获取缓存目录
     */
    public String cachePath() {
        return AppContext.getInstance().getDir(Constants.CACHE_HOME_DIR,
                Context.MODE_PRIVATE).getPath() + "/";
    }

}
