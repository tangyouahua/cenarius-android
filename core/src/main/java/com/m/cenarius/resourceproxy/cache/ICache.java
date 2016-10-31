package com.m.cenarius.resourceproxy.cache;

import com.m.cenarius.route.Route;

/**
 * 缓存池接口
 */
interface ICache {

    /**
     * 根据route返回相应的CacheEntry
     *
     * @param route 资源的route
     * @return 与该route匹配的缓存数据
     */
    CacheEntry findCache(Route route);

    /**
     * 移除单个缓存
     *
     * @param route 资源的route
     * @return 是否移除成功
     */
    boolean removeCache(Route route);

}
