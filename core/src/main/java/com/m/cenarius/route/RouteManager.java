package com.m.cenarius.route;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.m.cenarius.Cenarius;
import com.m.cenarius.Constants;
import com.m.cenarius.resourceproxy.cache.AssetCache;
import com.m.cenarius.resourceproxy.cache.InternalCache;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.FilesUtility;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.utils.Paths;
import com.m.cenarius.utils.Utils;
import com.m.cenarius.utils.io.FileUtils;
import com.m.cenarius.utils.io.IOUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * 管理route文件
 */
public class RouteManager {

    public static final String TAG = RouteManager.class.getSimpleName();

    public interface RouteRefreshCallback {

        /**
         * 更新过程的状态码
         */
        enum State {
            UPDATING_ERROR,//正在更新错误
            COPY_WWW,//拷贝www
            COPY_WWW_ERROR,//拷贝www出错
            DOWNLOAD_CONFIG,//下载配置文件
            DOWNLOAD_CONFIG_ERROR,//下载配置文件出错
            DOWNLOAD_ROUTES,//下载路由表
            DOWNLOAD_ROUTES_ERROR,//下载路由表出错
            DOWNLOAD_FILES,//下载文件
            DOWNLOAD_FILES_ERROR,//下载文件出错
            UPDATE_FILES_SUCCESS,//更新文件成功
        }


//        void onSuccess(String data);
//
//        void onFail();

        void onResult(State state, int process);
    }

    private static RouteManager instance;

    private RouteManager() {
//        loadLocalRoutes();
//        loadLocalConfig();
        BusProvider.getInstance().register(this);
    }

    private String wwwPath;

    /**
     * 获取H5加载目录
     */
    public static String getWWWPath() {
        return RouteManager.getInstance().wwwPath;
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
     * Routes
     */
    public static class Routes extends ArrayList<Route> {
    }

    /**
     * 最新的Route列表
     */
    public Routes routes;
    private String routesString;

    /**
     * config
     */
    private static class Config {
        String name;
        String ios_min_version;
        String android_min_version;
        String release;
    }

    /**
     * 最新的Config
     */
    public Config config;
    private String configString;

    /**
     * 缓存Route列表
     */
    public Routes cacheRoutes;

    /**
     * 资源Route列表
     */
    public Routes resourceRoutes;

    /**
     * 缓存Config列表
     */
    public Config cacheConfig;

    /**
     * 资源Config列表
     */
    public Config resourceConfig;

    /**
     * 进度
     */
    private int process;

//    /**
//     * 正在下载路由表
//     */
//    private boolean updatingRoutes;

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
//        this.remoteFolderUrl = remoteFolderUrl;
//        setRouteUrl(remoteFolderUrl + "/" + Constants.DEFAULT_DISK_ROUTES_FILE_NAME);
//        setConfigUrl(remoteFolderUrl + "/" + Constants.DEFAULT_DISK_CONFIG_FILE_NAME);
        this.remoteFolderUrl = remoteFolderUrl + "/";
        setRouteUrl(Constants.DEFAULT_DISK_ROUTES_FILE_NAME);
        setConfigUrl(Constants.DEFAULT_DISK_CONFIG_FILE_NAME);
    }

    /**
     * 加载本地的route
     * 1. 优先加载本地缓存；
     * 2. 如果没有本地缓存，则加载asset中预置的routes
     */
    private void loadLocalRoutes() {
        routes = null;
        cacheRoutes = null;
        resourceRoutes = null;
        // 读取 cacheRoutes
        String routeContent = readCachedRoutes();
        if (!TextUtils.isEmpty(routeContent)) {
            cacheRoutes = GsonHelper.getInstance().gson.fromJson(routeContent, Routes.class);
        }

        // 读取 resourceRoutes
        routeContent = readPresetRoutes();
        if (!TextUtils.isEmpty(routeContent)) {
            resourceRoutes = GsonHelper.getInstance().gson.fromJson(routeContent, Routes.class);
        }
    }

    /**
     * 加载本地的config
     * 1. 优先加载本地缓存；
     * 2. 如果没有本地缓存，则加载asset中预置的config
     */
    private void loadLocalConfig() {
        config = null;
        cacheConfig = null;
        resourceConfig = null;
        // 读取 cacheConfig
        String configContent = readCachedConfig();
        if (!TextUtils.isEmpty(configContent)) {
            cacheConfig = GsonHelper.getInstance().gson.fromJson(configContent, Config.class);
        }

        // 读取 resourceRoutes
        configContent = readPresetConfig();
        if (!TextUtils.isEmpty(configContent)) {
            resourceConfig = GsonHelper.getInstance().gson.fromJson(configContent, Config.class);
        }
    }


    /**
     * 刷新路由表
     */
    public void refreshRoute(final RouteRefreshCallback callback) {
        // 开发模式，直接成功
        if (Cenarius.DevelopModeEnable) {
            callback.onResult(RouteRefreshCallback.State.UPDATE_FILES_SUCCESS, 0);
            return;
        }

//        // 正在更新
//        if (isUpdatingRoutes()) {
//            callback.onResult(RouteRefreshCallback.State.UPDATING_ERROR, 0);
//            return;
//        }

        routeRefreshCallback = callback;
//        updatingRoutes = true;

        loadLocalConfig();
        loadLocalRoutes();
        downloadConfig();
//
//        RequestParams requestParams = new RequestParams(sRouteApi);
//        x.http().get(requestParams, new Callback.CommonCallback<String>() {
//
//            @Override
//            public void onSuccess(final String result) {
//                if (TextUtils.isEmpty(result)) {
//                    callback.onFail();
//                    updatingRoutes = false;
//                } else {
//                    //先更新内存中的 routes
//                    routes = JSON.parseArray(result, Route.class);
//
//                    //优先下载
//                    List<String> downloadFirstList = Cenarius.downloadFirstList;
//                    final List<Route> downloadFirstRoutes = new ArrayList<>();
//                    if (downloadFirstList != null) {
//                        for (String uri : downloadFirstList) {
//                            Route route = findRoute(uri);
//                            if (route != null) {
//                                downloadFirstRoutes.add(route);
//                            } else {
//                                //优先下载失败
//                                callback.onFail();
//                                updatingRoutes = false;
//                                return;
//                            }
//                        }
//                    }
//                    HtmlHelper.downloadFilesWithinRoutes(downloadFirstRoutes, true, new RouteRefreshCallback() {
//                        @Override
//                        public void onSuccess(String data) {
//
//                            if (cacheRoutes == null) {
//                                //优先下载成功，如果没有 cacheRoutes，立马保存
//                                cacheRoutes = routes;
//                                saveCachedRoutes(result);
//                            } else {
//                                //优先下载成功，把下载成功的 routes 加入 cacheRoutes 的最前面
//                                cacheRoutes.addAll(0, downloadFirstRoutes);
//                            }
//
////                            callback.onSuccess(null);
//                            BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_ROUTE_CHECK_VALID, null));
//
//                            //然后下载最新 routes 中的资源文件
//                            HtmlHelper.downloadFilesWithinRoutes(routes, false, new RouteRefreshCallback() {
//                                @Override
//                                public void onSuccess(String data) {
//                                    // 所有文件更新到最新，保存路由表
//                                    cacheRoutes = routes;
//                                    saveCachedRoutes(result);
//                                    updatingRoutes = false;
//                                }
//
//                                @Override
//                                public void onFail() {
//                                    updatingRoutes = false;
//                                }
//                            });
//
//                        }
//
//                        @Override
//                        public void onFail() {
//                            //优先下载失败
////                            callback.onFail();
//                            BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_ROUTE_CHECK_INVALID, null));
//                            updatingRoutes = false;
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                callback.onFail();
//                updatingRoutes = false;
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });
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

    /**
     * 存储缓存的Config
     *
     * @param content config
     */
    private void saveCachedConfig(final String content) {

        try {
            //保存新config
            File file = getCachedConfigFile();
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
                if (oldRoute.file.equals(newRoute.file)) {
                    isDeleted = false;
                    if (!newRoute.hash.equals(oldRoute.hash)) {
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
     * @return 读取缓存 route
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
     * @return 读取预置 route
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
     * @return 读取缓存 config
     */
    public String readCachedConfig() {
        File file = getCachedConfigFile();
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
     * @return 读取预置 config
     */
    public String readPresetConfig() {
        try {
            AssetManager assetManager = AppContext.getInstance()
                    .getAssets();
            InputStream inputStream = assetManager.open(Constants.PRESET_CONFIG_FILE_PATH);
            return IOUtils.toString(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * route路径
     */
    private File getCachedRoutesFile() {
        File fileDir = InternalCache.getInstance().fileDir();
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return new File(fileDir, Constants.DEFAULT_DISK_ROUTES_FILE_NAME);
    }

    /**
     * config路径
     */
    private File getCachedConfigFile() {
        File fileDir = InternalCache.getInstance().fileDir();
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return new File(fileDir, Constants.DEFAULT_DISK_CONFIG_FILE_NAME);
    }

    /**
     * 下载配置
     */
    private void downloadConfig() {
        routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_CONFIG, 0);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(remoteFolderUrl).build();
        DownloadService downloadService = retrofit.create(DownloadService.class);
        Call<ResponseBody> call = downloadService.downloadConfig(configUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    //下载config成功
                    try {
                        configString = response.body().string();
                        config = GsonHelper.getInstance().gson.fromJson(configString, Config.class);
                        if (hasMinVersion(config)) {
                            // 满足最小版本要求
                            if (shouldUpdateWWW(config)) {
                                // 需要更新www
                                if (isWWwFolderNeedsToBeInstalled()) {
                                    // 需要拷贝www
                                    copyAssetToData();
                                } else {
                                    // 不需要拷贝www
                                    downloadRoute();
                                }
                            } else {
                                // 不需要更新www
                                updateSuccess();
                            }
                        } else {
                            // 不满足最小版本要求
                            updateSuccess();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        //下载config失败
                        routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_CONFIG, 0);
                    }
                } else {
                    //下载config失败
                    routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_CONFIG, 0);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //下载config失败
                routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_CONFIG, 0);
            }
        });
    }

    private interface DownloadService {
        @GET
        Call<ResponseBody> downloadConfig(@Url String url);

        @GET
        Call<ResponseBody> downloadRoute(@Url String url);

        @GET
        Call<ResponseBody> downloadFile(@Url String url);
    }

    /**
     * 下载路由表
     */
    private void downloadRoute() {
        routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_ROUTES, 0);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(remoteFolderUrl).build();
        DownloadService downloadService = retrofit.create(DownloadService.class);
        Call<ResponseBody> call = downloadService.downloadRoute(routeUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    //下载route成功
                    try {
                        routesString = response.body().string();
                        routes = GsonHelper.getInstance().gson.fromJson(routesString, Routes.class);
//                        routes = new Gson().fromJson(routesString, new TypeToken<List<Route>>() {}.getType());
                        downloadFiles(routes);
                    } catch (IOException e) {
                        e.printStackTrace();
                        //下载route失败
                        routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_ROUTES_ERROR, 0);
                    }

                } else {
                    //下载route失败
                    routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_ROUTES_ERROR, 0);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //下载route失败
                routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_ROUTES_ERROR, 0);
            }
        });
    }

//    public class Route{
//        public String file;
//        public String hash;
//    }

    /**
     * 拷贝事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCopyWWW(BusProvider.BusEvent event) {
        if (event.eventId == Constants.BUS_EVENT_COPY_WWW_START) {
            // 开始拷贝www
            routeRefreshCallback.onResult(RouteRefreshCallback.State.COPY_WWW, 0);
        } else if (event.eventId == Constants.BUS_EVENT_COPY_WWW_SUCCESS) {
            // 拷贝www成功
            downloadRoute();
        } else if (event.eventId == Constants.BUS_EVENT_COPY_WWW_ERROR) {
            // 拷贝www失败
            routeRefreshCallback.onResult(RouteRefreshCallback.State.COPY_WWW_ERROR, 0);
        }
    }

    /**
     * 下载事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadFile(BusProvider.BusEvent event) {
        if (event.eventId == Constants.BUS_EVENT_DOWNLOAD_ALL_FILE_SUCCESS) {
            // 所有下载成功
            saveRouteAndConfig();
        } else if (event.eventId == Constants.BUS_EVENT_DOWNLOAD_FILE_ERROR) {
            // 下载失败
            routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_FILES_ERROR, 0);
        } else if (event.eventId == Constants.BUS_EVENT_DOWNLOAD_FILE_SUCCESS) {
            // 单个下载成功
            process = event.data.getInt("process");
            routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_FILES, process);
        }
    }

    /**
     * 下载文件
     */
    private void downloadFiles(Routes routes) {
        routeRefreshCallback.onResult(RouteRefreshCallback.State.DOWNLOAD_FILES, 0);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(remoteFolderUrl).build();
        DownloadService downloadService = retrofit.create(DownloadService.class);
        downloadFile(routes, 0, downloadService);
    }

    /**
     * 下载文件
     */
    private void downloadFile(final Routes routes, final int index, final DownloadService downloadService) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (routes == null || routes.isEmpty() || index >= routes.size()) {
                    // 下载完成
                    BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_DOWNLOAD_ALL_FILE_SUCCESS, null));
                    return;
                }
                // 进度
                Bundle data = new Bundle();
                data.putInt("process",(index+1) * 100 / routes.size());
                BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_DOWNLOAD_FILE_SUCCESS, data));
                final Route route = routes.get(index);
                if (shouldDownload(route)) {
                    // 需要下载
                    Call<ResponseBody> call = downloadService.downloadFile(route.file);
                    try {
                        ResponseBody responseBody = call.execute().body();
                        if (responseBody != null) {
                            // 下载成功，保存
                            InternalCache.getInstance().saveCache(route, responseBody.bytes());
                            downloadFile(routes, index + 1, downloadService);
                        } else {
                            // 下载失败
                            BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_DOWNLOAD_FILE_ERROR, null));
                        }
                    } catch (IOException e) {
                        // 下载失败
                        e.printStackTrace();
                        BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_DOWNLOAD_FILE_ERROR, null));
                    }
                } else {
                    // 不需要下载
                    downloadFile(routes, index + 1, downloadService);
                }
            }
        }).start();
    }

    /**
     * 保存路由和配置
     */
    private void saveRouteAndConfig() {
//        String routesString = GsonHelper.getInstance().gson.toJson(routes, new TypeToken<List<Route>>() {
//        }.getType());
        saveCachedRoutes(routesString);
//        String configString = GsonHelper.getInstance().gson.toJson(config, Config.class);
        saveCachedConfig(configString);
        routeRefreshCallback.onResult(RouteRefreshCallback.State.UPDATE_FILES_SUCCESS,0);
    }

    /**
     * 某个文件是否要更新
     */
    private boolean shouldDownload(Route route) {
        if (cacheRoutes != null) {
            for (Route cacheRoute : cacheRoutes) {
                if (route.equals(cacheRoute)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 是否满足最小版本要求
     */
    private boolean hasMinVersion(Config config) {
        String versionName = Utils.getAppVersionName();
        if (versionName != null && config.android_min_version != null && versionName.compareTo(config.android_min_version) >= 0) {
            // 满足最小版本要求
            return true;
        } else {
            // 不满足最小版本要求
            return false;
        }
    }

    /**
     * 是否需要安装www文件夹
     */
    private boolean isWWwFolderNeedsToBeInstalled() {
        if (cacheConfig == null || cacheConfig.release.compareTo(resourceConfig.release) < 0) {
            //没有缓存或者缓存比预置低
            return true;
        }
        return false;
    }

    /**
     * 是否需要更新www文件夹
     */
    private boolean shouldUpdateWWW(Config config) {
        if (isWWwFolderNeedsToBeInstalled()) {
            return config.release.compareTo(resourceConfig.release) > 0;
        } else {
            return config.release.compareTo(cacheConfig.release) > 0;
        }
    }

    /**
     * 更新成功
     */
    private void updateSuccess() {
        if (isWWwFolderNeedsToBeInstalled()) {
            // 从asset加载
            wwwPath = AssetCache.getInstance().assetsPath();
        } else {
            // 从data加载
            wwwPath = "file://" + InternalCache.getInstance().wwwCachePath();
        }
        // 成功，进APP
        routeRefreshCallback.onResult(RouteRefreshCallback.State.UPDATE_FILES_SUCCESS, 100);
    }

    /**
     * 把www文件夹安装到外部存储
     */
    private void copyAssetToData() {
        BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_COPY_WWW_START, null));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 拷贝文件
                    AssetManager assetManager = AppContext.getInstance().getResources().getAssets();
                    copyAssetDirectory(assetManager, Constants.DEFAULT_ASSET_FILE_PATH, InternalCache.getInstance().wwwCachePath());
                    BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_COPY_WWW_SUCCESS, null));
                } catch (IOException e) {
                    e.printStackTrace();
                    BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_COPY_WWW_ERROR, null));
                }
            }
        }).start();
    }

    private static void copyAssetDirectory(AssetManager assetManager, String fromDirectory, String toDirectory) throws IOException {
        // 重新创建文件夹
        FilesUtility.delete(toDirectory);
        FilesUtility.ensureDirectoryExists(toDirectory);

        // 拷贝 , 最后拷贝route和config
        String[] files = assetManager.list(fromDirectory);
        for (String file : files) {
            final String destinationFileAbsolutePath = Paths.get(toDirectory, file);
            final String assetFileAbsolutePath = Paths.get(fromDirectory, file).substring(1);
            String subFiles[] = assetManager.list(assetFileAbsolutePath);
            if (subFiles.length == 0) {
                copyAssetFile(assetManager, assetFileAbsolutePath, destinationFileAbsolutePath);
            } else {
                copyAssetDirectory(assetManager, assetFileAbsolutePath, destinationFileAbsolutePath);
            }
        }
    }

    /**
     * 拷贝本地www到外部www
     */
    private static void copyAssetFile(AssetManager assetManager, String assetFilePath, String destinationFilePath) throws IOException {
        InputStream in = assetManager.open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }


}
