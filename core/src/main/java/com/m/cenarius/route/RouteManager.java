package com.m.cenarius.route;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.m.cenarius.Cenarius;
import com.m.cenarius.Constants;
import com.m.cenarius.resourceproxy.cache.InternalCache;
import com.m.cenarius.resourceproxy.network.HtmlHelper;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.io.FileUtils;
import com.m.cenarius.utils.io.IOUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理route文件
 */
public class RouteManager {

    public static final String TAG = RouteManager.class.getSimpleName();

    public interface RouteRefreshCallback {

        /**
         * 更新过程的状态码
         */
        enum State{
            UPDATING_ERROR,//正在更新错误
            COPY_WWW,//拷贝www
            COPY_WWW_ERROR,//拷贝www出错
            DOWNLOAD_CONFIG,//下载配置文件
            DOWNLOAD_CONFIG_ERROR,//下载配置文件出错
            DOWNLOAD_ROUTES,//下载路由表
            DOWNLOAD_ROUTES_ERROR,//下载路由表出错
            UPDATE_FILES,//更新文件
            UPDATE_FILES_ERROR,//更新文件出错
            UPDATE_FILES_SUCCESS,//更新文件成功
        }



//        void onSuccess(String data);
//
//        void onFail();

        void onResult(State state, int process);
    }

    private static RouteManager instance;

    private RouteManager() {
        loadLocalRoutes();
        BusProvider.getInstance().register(this);
    }

    /**
     * Routes的远程地址
     */
    private static String routeUrl;

    /**
     * Config的远程地址
     */
    private static String configUrl;

    /**
     * 等待route刷新的callback
     */
    private RouteRefreshCallback routeRefreshCallback;

    /**
     * 最新的Route列表
     */
    public List<Route> routes;

    /**
     * 缓存Route列表
     */
    public List<Route> cacheRoutes;

    /**
     * 资源Route列表
     */
    public List<Route> resourceRoutes;

    /**
     * 正在下载路由表
     */
    private boolean updatingRoutes;

    /**
     * 远程目录 url
     */
    public String remoteFolderUrl;

    public static RouteManager getInstance() {
        if (null == instance) {
            synchronized (RouteManager.class) {
                if (null == instance) {
                    instance = new RouteManager();
                }
            }
        }
        return instance;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    /**
     * 设置routes地址
     */
    private void setRouteUrl(String routeUrl) {
        this.routeUrl = routeUrl;
    }

    /**
     * 设置config地址
     */
    private void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    /**
     * 设置远程资源地址
     */
    public void setRemoteFolderUrl(String remoteFolderUrl) {
        this.remoteFolderUrl = remoteFolderUrl;
        setRouteUrl(remoteFolderUrl + "/" + Constants.DEFAULT_DISK_ROUTES_FILE_NAME);
        setConfigUrl(remoteFolderUrl + "/" + Constants.DEFAULT_DISK_CONFIG_FILE_NAME);
    }

    /**
     * 加载本地的route
     * 1. 优先加载本地缓存；
     * 2. 如果没有本地缓存，则加载asset中预置的routes
     */
    private void loadLocalRoutes() {
        // 读取 cacheRoutes
        String routeContent = readCachedRoutes();
        if (!TextUtils.isEmpty(routeContent)) {
            cacheRoutes = JSON.parseArray(routeContent, Route.class);
        }

        // 读取 resourceRoutes
        routeContent = readPresetRoutes();
        if (!TextUtils.isEmpty(routeContent)) {
            resourceRoutes = JSON.parseArray(routeContent, Route.class);
        }
    }

    /**
     * 刷新路由表
     */
    public void refreshRoute(final RouteRefreshCallback callback) {
        routeRefreshCallback = callback;

        if (Cenarius.DevelopModeEnable) {
            callback.onResult(RouteRefreshCallback.State.UPDATE_FILES_SUCCESS, 0);
            return;
        }

        if (updatingRoutes) {
            callback.onResult(RouteRefreshCallback.State.UPDATING_ERROR, 0);
            return;
        }
        updatingRoutes = true;





        RequestParams requestParams = new RequestParams(sRouteApi);
        x.http().get(requestParams, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(final String result) {
                if (TextUtils.isEmpty(result)) {
                    callback.onFail();
                    updatingRoutes = false;
                } else {
                    //先更新内存中的 routes
                    routes = JSON.parseArray(result, Route.class);

                    //优先下载
                    List<String> downloadFirstList = Cenarius.downloadFirstList;
                    final List<Route> downloadFirstRoutes = new ArrayList<>();
                    if (downloadFirstList != null) {
                        for (String uri : downloadFirstList) {
                            Route route = findRoute(uri);
                            if (route != null) {
                                downloadFirstRoutes.add(route);
                            } else {
                                //优先下载失败
                                callback.onFail();
                                updatingRoutes = false;
                                return;
                            }
                        }
                    }
                    HtmlHelper.downloadFilesWithinRoutes(downloadFirstRoutes, true, new RouteRefreshCallback() {
                        @Override
                        public void onSuccess(String data) {

                            if (cacheRoutes == null) {
                                //优先下载成功，如果没有 cacheRoutes，立马保存
                                cacheRoutes = routes;
                                saveCachedRoutes(result);
                            } else {
                                //优先下载成功，把下载成功的 routes 加入 cacheRoutes 的最前面
                                cacheRoutes.addAll(0, downloadFirstRoutes);
                            }

//                            callback.onSuccess(null);
                            BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_ROUTE_CHECK_VALID, null));

                            //然后下载最新 routes 中的资源文件
                            HtmlHelper.downloadFilesWithinRoutes(routes, false, new RouteRefreshCallback() {
                                @Override
                                public void onSuccess(String data) {
                                    // 所有文件更新到最新，保存路由表
                                    cacheRoutes = routes;
                                    saveCachedRoutes(result);
                                    updatingRoutes = false;
                                }

                                @Override
                                public void onFail() {
                                    updatingRoutes = false;
                                }
                            });

                        }

                        @Override
                        public void onFail() {
                            //优先下载失败
//                            callback.onFail();
                            BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_ROUTE_CHECK_INVALID, null));
                            updatingRoutes = false;
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                callback.onFail();
                updatingRoutes = false;
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * uri 是否在白名单中
     */
    public boolean isInWhiteList(String uri) {
        for (String path : Cenarius.routesWhiteList) {
            if (uri.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 找到能够解析uri的Route
     *
     * @param uri 需要处理的uri
     * @return 能够处理uri的Route，如果没有则为null
     */
    public Route findRoute(String uri) {
        uri = deleteSlash(uri);
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        if (null == routes) {
            return null;
        }
        for (Route route : routes) {
            if (route.match(uri)) {
                return route;
            }
        }
        return null;
    }

    /**
     * 删除多余 /
     */
    public String deleteSlash(String uri) {
        if (uri.contains("//")) {
            uri = uri.replace("//", "/");
            uri = deleteSlash(uri);
        }
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        return uri;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshRoute(BusProvider.BusEvent event) {
        if (event.eventId == Constants.BUS_EVENT_ROUTE_CHECK_VALID) {
            mRouteRefreshCallback.onSuccess(null);
        } else if (event.eventId == Constants.BUS_EVENT_ROUTE_CHECK_INVALID) {
            mRouteRefreshCallback.onFail();
        }
    }

    /**
     * 删除缓存的Routes
     */
    public boolean deleteCachedRoutes() {
        File file = getCachedRoutesFile();
        boolean result = file.exists() && file.delete();
//        if (result) {
//            loadLocalRoutes();
//        }
        return result;
    }

    /**
     * 存储缓存的Routes
     *
     * @param content route内容
     */
    private void saveCachedRoutes(final String content) {

        try {
            //保存新routes
            File file = getCachedRoutesFile();
            if (file.exists()) {
                file.delete();
            }
            FileUtils.write(file, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteOldFiles(ArrayList<Route> newRoutes, ArrayList<Route> oldRoutes) {
        //找到需要删除的和更新的文件
        ArrayList<Route> changedRoutes = new ArrayList<>();
        ArrayList<Route> deletedRoutes = new ArrayList<>();
        for (Route oldRoute : oldRoutes) {
            boolean isDeleted = true;
            for (Route newRoute : newRoutes) {
                if (oldRoute.uri.equals(newRoute.uri)) {
                    isDeleted = false;
                    if (!newRoute.fileHash.equals(oldRoute.fileHash)) {
                        changedRoutes.add(oldRoute);
                    }
                }
            }
            if (isDeleted) {
                deletedRoutes.add(oldRoute);
            }
        }

        deletedRoutes.addAll(changedRoutes);
        for (Route route : deletedRoutes) {
            InternalCache.getInstance().removeCache(route);
        }
    }

    /**
     * @return 读取缓存的route
     */
    public String readCachedRoutes() {
        File file = getCachedRoutesFile();
        if (!file.exists()) {
            return null;
        }
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return 读取preset routes
     */
    public String readPresetRoutes() {
        try {
            AssetManager assetManager = AppContext.getInstance()
                    .getAssets();
            InputStream inputStream = assetManager.open(Constants.PRESET_ROUTE_FILE_PATH);
            return IOUtils.toString(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 存储文件路径
     *
     * @return
     */
    private File getCachedRoutesFile() {
        File fileDir = AppContext.getInstance().getDir(Constants.CACHE_HOME_DIR,
                Context.MODE_PRIVATE);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return new File(fileDir, Constants.DEFAULT_DISK_ROUTES_FILE_NAME);
    }

    /**
     * 通过本地的Routes能否处理uri
     *
     * @return
     */
    public boolean handleByNative(String uri) {
        if (TextUtils.isEmpty(uri)) {
            return false;
        }
        return findRoute(uri) != null;
    }

//    /**
//     * 如果本地的Routes不能处理uri，会尝试更新Routes来处理
//     *
//     * @return
//     */
//    public void handleRemote(final String uri, final UriHandleCallback callback) {
//        if (null == callback) {
//            return;
//        }
//        RouteManager.getInstance().refreshRoute(new RouteManager.RouteRefreshCallback() {
//            @Override
//            public void onSuccess(String data) {
//                callback.onResult(handleByNative(uri));
//            }
//
//            @Override
//            public void onFail() {
//                callback.onResult(false);
//            }
//        });
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventMainThread(BusProvider.BusEvent event) {
//        if (null == event) {
//            return;
//        }
//
//        if (event.eventId == Constants.BUS_EVENT_ROUTE_CHECK_VALID) {
//            if (null != mRouteRefreshCallback) {
//                mRouteRefreshCallback.onSuccess(mCheckingRouteString);
//            }
//            LogUtils.i(TAG, "new route effective");
//        }
//        else if (event.eventId == Constants.BUS_EVENT_ROUTE_CHECK_INVALID){
//            if (null != mRouteRefreshCallback) {
//                mRouteRefreshCallback.onFail();
//            }
//        }
//    }

    public boolean isUpdatingRoutes() {
        return updatingRoutes;
    }

}
