package com.m.cenarius.utils;

import org.xutils.HttpManager;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.HttpTask;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;

/**
 * 自定义的 HttpManager，用于签名
 */

public class XutilsHttpManager implements HttpManager {
    private static final Object lock = new Object();
    private static volatile XutilsHttpManager instance;

    private XutilsHttpManager() {
    }

    public static void registerInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new XutilsHttpManager();
                }
            }
        }
        x.Ext.setHttpManager(instance);
    }

    @Override
    public <T> Callback.Cancelable get(RequestParams entity, Callback.CommonCallback<T> callback) {
        return request(HttpMethod.GET, entity, callback);
    }

    @Override
    public <T> Callback.Cancelable post(RequestParams entity, Callback.CommonCallback<T> callback) {
        return request(HttpMethod.POST, entity, callback);
    }

    @Override
    public <T> Callback.Cancelable request(HttpMethod method, RequestParams entity, Callback.CommonCallback<T> callback) {
        entity.setMethod(method);
        Callback.Cancelable cancelable = null;
        if (callback instanceof Callback.Cancelable) {
            cancelable = (Callback.Cancelable) callback;
        }
        XutilsInterceptor.openApiForRequestParams(entity);
        HttpTask<T> task = new HttpTask<T>(entity, cancelable, callback);
        return x.task().start(task);
    }

    @Override
    public <T> T getSync(RequestParams entity, Class<T> resultType) throws Throwable {
        return requestSync(HttpMethod.GET, entity, resultType);
    }

    @Override
    public <T> T postSync(RequestParams entity, Class<T> resultType) throws Throwable {
        return requestSync(HttpMethod.POST, entity, resultType);
    }

    @Override
    public <T> T requestSync(HttpMethod method, RequestParams entity, Class<T> resultType) throws Throwable {
        XutilsHttpManager.DefaultSyncCallback<T> callback = new XutilsHttpManager.DefaultSyncCallback<T>(resultType);
        return requestSync(method, entity, callback);
    }

    @Override
    public <T> T requestSync(HttpMethod method, RequestParams entity, Callback.TypedCallback<T> callback) throws Throwable {
        entity.setMethod(method);
        XutilsInterceptor.openApiForRequestParams(entity);
        HttpTask<T> task = new HttpTask<T>(entity, null, callback);
        return x.task().startSync(task);
    }

    private class DefaultSyncCallback<T> implements Callback.TypedCallback<T> {

        private final Class<T> resultType;

        public DefaultSyncCallback(Class<T> resultType) {
            this.resultType = resultType;
        }

        @Override
        public Type getLoadType() {
            return resultType;
        }

        @Override
        public void onSuccess(T result) {

        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {

        }

        @Override
        public void onCancelled(CancelledException cex) {

        }

        @Override
        public void onFinished() {

        }
    }
}
