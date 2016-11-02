package com.m.cenarius.resourceproxy.cache;

import android.content.Context;

import com.m.cenarius.Constants;
import com.m.cenarius.route.Route;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.LogUtils;

import java.io.File;

public class CacheHelper {

    public static final String TAG = CacheHelper.class.getSimpleName();

    private static CacheHelper sInstance;

    private CacheHelper() {
        if (null == mInternalCache) {
            mInternalCache = new InternalCache();
        }
        if (null == mAssetCache){
            mAssetCache = AssetCache.getInstance();
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

    private InternalCache mInternalCache;
    private AssetCache mAssetCache;
//    /**
//     * register cache
//     */
//    private List<ICache> mCaches = new ArrayList<>();
    /**
     * 是否使用缓存
     */
    private boolean mCacheEnabled = true;

//    /**
//     * Register additional readable cache
//     *
//     * @param cache
//     */
//    public void registerCache(ICache cache) {
//        if (null != cache) {
//            mCaches.add(cache);
//        }
//    }

    /**
     * 查找 uri 对应的本地 html 文件 URL。先查 Cache，再查 asset
     */
    public String localHtmlURLForURI(String uri){
        Route route = RouteManager.getInstance().findRoute(uri);
        if (null == route)
        {
            LogUtils.i(TAG, "route not found");
        }
        else {
            File cacheFile = mInternalCache.file(route);
            if (cacheFile.exists() && cacheFile.canRead()){
                return cacheFile.getPath();
            }
            CacheEntry cacheEntry = mAssetCache.findCache(route);
            if (cacheEntry != null){
                return mAssetCache.fileUrl(route);
            }
        }
        return null;
    }

    /**
     * 查找 uri 对应的服务器上 html 文件。
     */
    public String remoteHtmlURLForURI(String uri){
        Route route = RouteManager.getInstance().findRoute(uri);
        if (null == route)
        {
            LogUtils.i(TAG, "route not found");
        }
        else {
            return route.getHtmlFile();
        }
        return null;
    }

    /**
     * 查找缓存
     *
     * @param route
     * @return
     */
    public CacheEntry findCache(Route route) {
//        // 查找缓存
//        if (route.uri.contains(Constants.EXTENSION_HTML)) {
//            return findHtmlCache(route);
//        }
//        CacheEntry result = null;
//        // 遍历内部缓存
//        result = mInternalCache.findCache(route);
//        if (null != result) {
//            return result;
//        }
//        // 遍历外部缓存
//        for (ICache cache : mCaches) {
//            result = cache.findCache(route);
//            if (null != result && result.isValid()) {
//                return result;
//            }
//        }
//        return result;
        CacheEntry result = mInternalCache.findCache(route);
        return result;
    }

//    /**
//     * 查找html缓存
//     *
//     * @param route
//     * @return
//     */
//    public CacheEntry findHtmlCache(Route route) {
//        CacheEntry result;
//        // 遍历外部缓存
//        for (ICache cache : mCaches) {
//            result = cache.findCache(route);
//            if (null != result && result.isValid()) {
//                return result;
//            }
//        }
//        // 遍历内部缓存
//        result = mInternalHtmlCache.findCache(route);
//        if (null != result) {
//            return result;
//        }
//        return null;
//    }

//    /**
//     * Just save to internalCache
//     *
//     * @param route
//     * @param bytes
//     */
//    public void saveCache(Route route, byte[] bytes) {
//        if (null == bytes || bytes.length == 0) {
//            return;
//        }
//        mInternalCache.putCache(route, bytes);
//    }

    /**
     * 保存
     *
     * @param route route
     * @param bytes
     */
    public boolean saveCache(Route route, byte[] bytes) {
        if (null == bytes || bytes.length == 0) {
            return false;
        }
        return mInternalCache.saveCache(route, bytes);
    }

    /**
     * Clear caches
     */
    public void clearCache() {
        mInternalCache.clear();
    }

    /**
     * 删除单个资源缓存
     *
     * @param route 资源地址
     */
    public void removeCache(Route route) {
        mInternalCache.removeCache(route);
    }

//    /**
//     * 删除单个html缓存
//     *
//     * @param route route
//     */
//    public void removeHtmlCache(Route route) {
//        mInternalHtmlCache.removeCache(route);
//    }

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



}
