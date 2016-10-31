package com.m.cenarius.resourceproxy.cache;

import android.content.Context;

import com.m.cenarius.Constants;
import com.m.cenarius.route.Route;
import com.m.cenarius.utils.AppContext;

import java.util.ArrayList;
import java.util.List;


public class CacheHelper {

    public static final String TAG = CacheHelper.class.getSimpleName();

    private static CacheHelper sInstance;

    private CacheHelper() {
        if (null == mInternalCache) {
            mInternalCache = new InternalCache();
        }
        if (null == mInternalHtmlCache) {
            mInternalHtmlCache = new HtmlFileCache();
        }
    }

    public static CacheHelper getInstance() {
        if (null == sInstance) {
            synchronized (CacheHelper.class) {
                if (null == sInstance) {
                    sInstance = new CacheHelper();
                }
            }
        }
        return sInstance;
    }

    /**
     * internal cache
     */
    private InternalCache mInternalCache;
    private HtmlFileCache mInternalHtmlCache;
    /**
     * register cache
     */
    private List<ICache> mCaches = new ArrayList<>();
    /**
     * 是否使用缓存
     */
    private boolean mCacheEnabled = true;

    /**
     * Register additional readable cache
     *
     * @param cache
     */
    public void registerCache(ICache cache) {
        if (null != cache) {
            mCaches.add(cache);
        }
    }

    /**
     * 查找缓存
     *
     * @param route
     * @return
     */
    public CacheEntry findCache(Route route) {
        // 如果是html文件，则查找html缓存
        if (route.uri.contains(Constants.EXTENSION_HTML)) {
            return findHtmlCache(route);
        }
        CacheEntry result = null;
        // 遍历内部缓存
        result = mInternalCache.findCache(route);
        if (null != result) {
            return result;
        }
        // 遍历外部缓存
        for (ICache cache : mCaches) {
            result = cache.findCache(route);
            if (null != result && result.isValid()) {
                return result;
            }
        }
        return result;
    }

    /**
     * 查找html缓存
     *
     * @param route
     * @return
     */
    public CacheEntry findHtmlCache(Route route) {
        CacheEntry result;
        // 遍历外部缓存
        for (ICache cache : mCaches) {
            result = cache.findCache(route);
            if (null != result && result.isValid()) {
                return result;
            }
        }
        // 遍历内部缓存
        result = mInternalHtmlCache.findCache(route);
        if (null != result) {
            return result;
        }
        return null;
    }

    /**
     * Just save to internalCache
     *
     * @param route
     * @param bytes
     */
    public void saveCache(Route route, byte[] bytes) {
        if (null == bytes || bytes.length == 0) {
            return;
        }
        mInternalCache.putCache(route, bytes);
    }

    /**
     * Just save html file
     *
     * @param route route
     * @param bytes
     */
    public boolean saveHtmlCache(Route route, byte[] bytes) {
        if (null == bytes || bytes.length == 0) {
            return false;
        }
        return mInternalHtmlCache.saveCache(route, bytes);
    }

    /**
     * Clear caches
     */
    public void clearCache() {
        // clear file caches
        mInternalCache.clear();
        mInternalCache = new InternalCache();
        // clear html files
        mInternalHtmlCache.clear();
    }

    /**
     * 删除单个资源缓存
     *
     * @param route 资源地址
     */
    public void removeInternalCache(Route route) {
        mInternalCache.removeCache(route);
    }

    /**
     * 删除单个html缓存
     *
     * @param route route
     */
    public void removeHtmlCache(Route route) {
        mInternalHtmlCache.removeCache(route);
    }

//    /**
//     * 是否能够缓存
//     *
//     * @param url
//     * @return
//     */
//    public boolean checkUrl(String url) {
//        if (TextUtils.isEmpty(url)) {
//            LogUtils.i(TAG, "can not cache, url = " + url);
//            return false;
//        }
//
//        // 获取文件名
//        String fileName;
//        if (!url.contains(File.separator)) {
//            fileName = url;
//        } else {
//            fileName = Uri.parse(url)
//                    .getLastPathSegment();
//            if (TextUtils.isEmpty(fileName)) {
//                fileName = Uri.parse(url)
//                        .getHost();
//            }
//        }
//        // 如果文件名为空，则不能缓存
//        if (TextUtils.isEmpty(fileName)) {
//            LogUtils.i(TAG, "can not cache, fileName is null, url = " + url);
//            return false;
//        }
//        // 如果文件名不为空，且后缀为能够缓存的类型，则可以缓存
//        for (String extension : Constants.CACHE_FILE_EXTENSION) {
//            if (fileName.endsWith(extension)) {
//                LogUtils.i(TAG, "can cache url = " + url);
//                return true;
//            }
//        }
//        // 默认不能缓存
//        LogUtils.i(TAG, "can not cache, extension not match, url = " + url);
//        return false;
//    }

    /**
     * 禁用缓存
     */
    public void enableCache(boolean enableCache) {
        mCacheEnabled = enableCache;
    }

    public boolean cacheEnabled() {
        return mCacheEnabled;
    }

    /**
     * 获取缓存目录
     */
    public String cachePath(){
        return AppContext.getInstance().getDir(Constants.CACHE_HOME_DIR,
                Context.MODE_PRIVATE).getPath() + "/";
    }

    /**
     * 获取assets目录
     */
    public String assetsPath(){
        return "file:///android_asset/";
    }

//    /**
//     * 根据fileHash获取缓存的key
//     *
//     * @param fileHash
//     * @return
//     */
//    public String urlToKey(String fileHash) {
//        // fileHash为空,返回null
//        if (TextUtils.isEmpty(fileHash)) {
//            return null;
//        }
//        try {
//            // 如果只有文件名
//            if (!url.contains(File.separator)) {
//                return url;
//            }
//            Uri uri = Uri.parse(url);
//            String path = uri.getPath();
//            String key = Utils.hash(path);
//            LogUtils.i(TAG, "url : " + url + " ; key : " + key);
//            return key;
//        } catch (Exception e) {
//            LogUtils.e(TAG, e.getMessage());
//        }
//
//        return null;
//    }
}
