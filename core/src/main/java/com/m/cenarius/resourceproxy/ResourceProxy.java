package com.m.cenarius.resourceproxy;

import com.m.cenarius.resourceproxy.cache.AssetCache;
import com.m.cenarius.resourceproxy.cache.CacheHelper;
import com.m.cenarius.resourceproxy.network.HtmlHelper;
import com.m.cenarius.resourceproxy.network.INetwork;
import com.m.cenarius.resourceproxy.network.NetworkImpl;
import com.m.cenarius.route.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * ResourceProxy负责资源管理，比如获取缓存的资源，写入缓存资源，请求线上资源
 */
public class ResourceProxy {

    public static final String TAG = ResourceProxy.class.getSimpleName();

    private static ResourceProxy sInstance;
    private INetwork mNetwork;
    private List<String> mProxyHosts = new ArrayList<>();

    private ResourceProxy(){
        CacheHelper.getInstance().registerCache(AssetCache.getInstance());
    }
    public static ResourceProxy getInstance() {
        if (null == sInstance) {
            synchronized (ResourceProxy.class) {
                if (null == sInstance) {
                    sInstance = new ResourceProxy();
                }
            }
        }
        return sInstance;
    }

    public ResourceProxy enableCache(boolean enableCache) {
        CacheHelper.getInstance().enableCache(enableCache);
        return this;
    }

    /**
     * 预加载html
     */
    public void prepareHtmlFiles(ArrayList<Route> routes) {
        HtmlHelper.prepareHtmlFiles(routes);
    }

    public void clearCache() {
        CacheHelper.getInstance().clearCache();
    }

    public INetwork getNetwork() {
        if (null == mNetwork) {
            mNetwork = new NetworkImpl();
        }
        return mNetwork;
    }

    public void addProxyHosts(List<String> hosts) {
        if (null != hosts && !hosts.isEmpty()) {
            mProxyHosts.addAll(hosts);
        }
    }

    public List<String> getProxyHosts() {
        return mProxyHosts;
    }

}
