package com.m.cenarius.route;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.m.cenarius.Cenarius;
import com.m.cenarius.Constants;
import com.m.cenarius.resourceproxy.ResourceProxy;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.utils.io.FileUtils;
import com.m.cenarius.utils.io.IOUtils;
import com.google.gson.reflect.TypeToken;
import com.mcxiaoke.next.task.SimpleTaskCallback;
import com.mcxiaoke.next.task.TaskBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * 管理route文件
 */
public class RouteManager {

    public static final String TAG = RouteManager.class.getSimpleName();

    public interface RouteRefreshCallback {
        /**
         * @param data raw data
         */
        void onSuccess(String data);

        void onFail();
    }

    public interface UriHandleCallback {
        void onResult(boolean handle);
    }

    private static RouteManager sInstance;

    private RouteManager() {
        loadCachedRoutes();
        BusProvider.getInstance().register(this);
    }

    /**
     * 缓存Route列表
     */
    private ArrayList<Route> mRoutes;

    /**
     * 待校验的route数据
     */
    private String mCheckingRouteString;

    /**
     * 等待route刷新的callback
     */
    private RouteRefreshCallback mRouteRefreshCallback;

    /**
     * 远程目录 url
     */
    public String remoteFolderUrl;

    public static RouteManager getInstance() {
        if (null == sInstance) {
            synchronized (RouteManager.class) {
                if (null == sInstance) {
                    sInstance = new RouteManager();
                }
            }
        }
        return sInstance;
    }

    public ArrayList getRoutes() {
        return mRoutes;
    }

    /**
     * 设置获取routes地址
     */
    private void setRouteApi(String routeUrl) {
        if (!TextUtils.isEmpty(routeUrl)) {
            RouteFetcher.setRouteApi(routeUrl);
        }
    }

    /**
     * 设置远程资源地址
     */
    public void setRemoteFolderUrl(String remoteFolderUrl) {
        this.remoteFolderUrl = remoteFolderUrl;
        setRouteApi(remoteFolderUrl + "/" + Constants.DEFAULT_DISK_ROUTES_FILE_NAME);
    }

    /**
     * 加载本地的route
     * 1. 优先加载本地缓存；
     * 2. 如果没有本地缓存，则加载asset中预置的routes
     */
    private void loadCachedRoutes() {
        TaskBuilder.create(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // load cached routes
                try {
                    String routeContent = readCachedRoutes();
                    if (!TextUtils.isEmpty(routeContent)) {
                        mRoutes = GsonHelper.getInstance().fromJson(routeContent, new TypeToken<ArrayList<Route>>() {
                        }.getType());
                    }
                } catch (Exception e) {
                    LogUtils.i(TAG, e.getMessage());
                }

                // load preset routes
                if (null == mRoutes) {
                    try {
                        String routeContent = readPresetRoutes();
                        if (!TextUtils.isEmpty(routeContent)) {
                            mRoutes = GsonHelper.getInstance().fromJson(routeContent, new TypeToken<ArrayList<Route>>(){}.getType());
                        }
                    } catch (Exception e) {
                        LogUtils.i(TAG, e.getMessage());
                    }
                }

                return null;
            }
        }, new SimpleTaskCallback<Void>() {
        }, this).start();
    }

    /**
     * 以string方式返回Route列表, 如果Route为空则返回null
     */
    public String getRoutesString() {
        if (null == mRoutes) {
            return null;
        }
        return GsonHelper.getInstance().toJson(mRoutes);
    }

    /**
     * 找到能够解析uri的Route
     *
     * @param uri 需要处理的uri
     * @return 能够处理uri的Route，如果没有则为null
     */
    public Route findRoute(String uri) {
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        if (null == mRoutes) {
            return null;
        }
        for (Route route : mRoutes) {
            if (route.match(uri)) {
                return route;
            }
        }
        return null;
    }

    /**
     * 刷新路由表
     */
    public void refreshRoute(final RouteRefreshCallback callback) {
        RouteFetcher.fetchRoutes(new RouteRefreshCallback() {
            @Override
            public void onSuccess(String data) {
                mCheckingRouteString = data;
                mRouteRefreshCallback = callback;
                // prepare h5 files
                try {
                    ArrayList<Route> routes = GsonHelper.getInstance().fromJson(mCheckingRouteString, new TypeToken<ArrayList<Route>>() {
                    }.getType());
                    ResourceProxy.getInstance().prepareHtmlFiles(routes);
                } catch (Exception e) {
                    LogUtils.e(TAG, e.getMessage());
                    if (null != callback) {
                        callback.onFail();
                    }
                }
            }

            @Override
            public void onFail() {
                if (null != callback) {
                    callback.onFail();
                }
            }
        });
    }

    /**
     * 删除缓存的Routes
     */
    public boolean deleteCachedRoutes() {
        File file = getCachedRoutesFile();
        return file.exists() && file.delete();
    }

    /**
     * 存储缓存的Routes
     *
     * @param content route内容
     */
    private void saveCachedRoutes(final String content) {
        TaskBuilder.create(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                File file = getCachedRoutesFile();
                if (file.exists()) {
                    file.delete();
                }
                try {
                    if (TextUtils.isEmpty(content)) {
                        // 如果内容为空，则只删除文件
                        return null;
                    }
                    FileUtils.write(file, content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }, null, this).start();
    }

    /**
     * @return 读取缓存的route
     */
    private String readCachedRoutes() {
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

    /**
     * 如果本地的Routes不能处理uri，会尝试更新Routes来处理
     *
     * @return
     */
    public void handleRemote(final String uri, final UriHandleCallback callback) {
        if (null == callback) {
            return;
        }
        RouteManager.getInstance().refreshRoute(new RouteManager.RouteRefreshCallback() {
            @Override
            public void onSuccess(String data) {
                callback.onResult(handleByNative(uri));
            }

            @Override
            public void onFail() {
                callback.onResult(false);
            }
        });
    }

    public void onEventMainThread(BusProvider.BusEvent event) {
        if (null == event) {
            return;
        }

        if (event.eventId == Constants.BUS_EVENT_ROUTE_CHECK_VALID && !TextUtils.isEmpty(mCheckingRouteString)) {
            saveCachedRoutes(mCheckingRouteString);
            try {
                mRoutes = GsonHelper.getInstance().fromJson(mCheckingRouteString, new TypeToken<ArrayList<Route>>() {
                }.getType());
            } catch (Exception e) {
                LogUtils.e(TAG, e.getMessage());
            }
            if (null != mRouteRefreshCallback) {
                mRouteRefreshCallback.onSuccess(mCheckingRouteString);
            }
            LogUtils.i(TAG, "new route effective");
        }
    }
}
