package com.m.cenarius.resourceproxy.cache;

import android.net.Uri;

import com.m.cenarius.route.Route;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.LogUtils;

import java.io.File;

public class CacheHelper {

    public static final String TAG = CacheHelper.class.getSimpleName();

    private static CacheHelper sInstance;

    private CacheHelper() {
        if (null == mInternalCache) {
            mInternalCache = InternalCache.getInstance();
        }
        if (null == mAssetCache) {
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

    public String routeFileURLForRoute(Route route) {
        if (route == null) {
            LogUtils.i(TAG, "route not found");
            return null;
        }
        String cacheRouteFileURL = cacheRouteFileURLForRoute(route);
        if (cacheRouteFileURL != null) {
            return cacheRouteFileURL;
        }
        String resourceRouteFileURL = resourceRouteFileURLForRoute(route);
        if (resourceRouteFileURL != null) {
            return resourceRouteFileURL;
        }
        return null;
    }

    private String cacheRouteFileURLForRoute(Route route) {
        //路由表正在更新的时候需要对比 hash
        RouteManager routeManager = RouteManager.getInstance();
        if (routeManager.cacheRoutes != null && routeManager.cacheRoutes != routeManager.routes) {
            for (Route cacheRoute : routeManager.cacheRoutes) {
                if (cacheRoute.fileHash.equals(route.fileHash)) {
                    return cacheRouteFilePathForRoute(route);
                }
            }
            return null;
        } else {
            return cacheRouteFilePathForRoute(route);
        }
    }

    private String cacheRouteFilePathForRoute(Route route) {
        CacheEntry cacheEntry = mInternalCache.findCache(route);
        if (cacheEntry != null) {
            File cacheFile = mInternalCache.file(route);
            cacheEntry.close();
            return "file://" + cacheFile.getPath();
        }
        InternalCache.getInstance().removeCache(route);
        return null;
    }

    private String resourceRouteFileURLForRoute(Route route) {
        CacheEntry cacheEntry = mAssetCache.findCache(route);
        if (cacheEntry != null) {
            cacheEntry.close();
            return mAssetCache.fileUrl(route);
        }
        return null;
    }

    public String finalUrl(String url, Uri uri) {
        if (url != null) {
            String query = uri.getQuery();
            String fragment = uri.getFragment();
            if (query != null) {
                url = url + "?" + query;
            }
            if (fragment != null) {
                url = url + "#" + fragment;
            }
        }

        return url;
    }

    /**
     * 查找 uri 对应的本地 html 文件 URL。先查 Cache，再查 asset。如果在缓存文件和资源文件中都找不到对应的本地文件，返回 null
     */
    public String localHtmlURLForURI(String uriString) {
        if (uriString == null) {
            return null;
        }
        Uri finalUri = Uri.parse(uriString);
        String baseUri = finalUri.getPath();
        //最新的在内存中的 route
        Route route = RouteManager.getInstance().findRoute(baseUri);
        String urlString = routeFileURLForRoute(route);
        String finalUrl = finalUrl(urlString, finalUri);
        return finalUrl;
    }

    /**
     * 查找 uri 对应的服务器上 html 文件。
     */
    public String remoteHtmlURLForURI(String uri) {
        String remoteHTML = RouteManager.getInstance().remoteFolderUrl + "/" + uri;
        return remoteHTML;
    }

}
