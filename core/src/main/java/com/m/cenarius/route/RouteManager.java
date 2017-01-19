package com.m.cenarius.route;

import android.content.res.AssetManager;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;
import com.m.cenarius.Cenarius;
import com.m.cenarius.Constants;
import com.m.cenarius.resourceproxy.cache.InternalCache;
import com.m.cenarius.utils.AppContext;
import com.m.cenarius.utils.BusProvider;
import com.m.cenarius.utils.FilesUtility;
import com.m.cenarius.utils.GsonHelper;
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

        void onResult(State state, int process);
    }

    private static RouteManager instance;

    private RouteManager() {
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

//    /**
//     * Routes
//     */
//    public static class Routes extends ArrayList<Route> {
//    }

    /**
     * 最新的Route列表
     */
    private List<Route> routes;
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
    private List<Route> cacheRoutes;

    /**
     * 资源Route列表
     */
    private List<Route> resourceRoutes;

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

    /**
     * 拷贝份数
     */
    private int copyFileCount;

    /**
     * 下载份数份数
     */
    private int downloadFileCount;

    /**
     * 当前状态
     */
    private RouteRefreshCallback.State mState;

    /**
     * 当前进度
     */
    private int mProcess;

    /**
     * 需要下载www
     */
    private boolean shouldDownloadWWW;

    /**
     * 远程目录 url
     */
    public String remoteFolderUrl;

    /**
     * 缓存路由表的数据库
     */
    private LiteOrm liteOrm;

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

    /**
     * 设置routes地址
     */
    private void setRouteUrl(String routeUrl) {
        RouteManager.routeUrl = routeUrl;
    }

    /**
     * 设置config地址
     */
    private void setConfigUrl(String configUrl) {
        RouteManager.configUrl = configUrl;
    }

    /**
     * 设置远程资源地址
     */
    public void setRemoteFolderUrl(String remoteFolderUrl) {
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
        cacheRoutes = null;
        resourceRoutes = null;
        // 读取 cacheRoutes
//        String routeContent = readCachedRoutes();
//        if (!TextUtils.isEmpty(routeContent)) {
//            cacheRoutes = GsonHelper.getInstance().gson.fromJson(routeContent, Routes.class);
//        }
        cacheRoutes = liteOrm.query(Route.class);

        // 读取 resourceRoutes
        String routeContent = readPresetRoutes();
        if (!TextUtils.isEmpty(routeContent)) {
            resourceRoutes = GsonHelper.getInstance().gson.fromJson(routeContent, new TypeToken<List<Route>>(){}.getType());
        }
    }

    /**
     * 加载本地的config
     * 1. 优先加载本地缓存；
     * 2. 如果没有本地缓存，则加载asset中预置的config
     */
    private void loadLocalConfig() {
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

        // 重置变量
        routes = null;
        config = null;
        process = 0;
        copyFileCount = 0;
        downloadFileCount = 0;
        routeRefreshCallback = callback;

        if (liteOrm == null) {
            DataBaseConfig dataBaseConfig = new DataBaseConfig(AppContext.getInstance(), Constants.DEFAULT_DISK_ROUTES_DB_NAME);
            liteOrm = LiteOrm.newSingleInstance(dataBaseConfig);
            liteOrm.setDebugged(Cenarius.DEBUG);
        }

        loadLocalConfig();
        loadLocalRoutes();
        downloadConfig();
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
        return result;
    }

    /**
     * 删除缓存的Config
     */
    public boolean deleteCachedConfig() {
        File file = getCachedConfigFile();
        boolean result = file.exists() && file.delete();
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
        setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_CONFIG, 0);
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
                        shouldDownloadWWW = shouldDownloadWWW(config);
                        if (isWWwFolderNeedsToBeInstalled()) {
                            // 需要拷贝www
                            unzipAssetToData();
                        } else if (shouldDownloadWWW) {
                            // 下载路由表
                            downloadRoute();
                        } else {
                            // 不需要更新www
                            updateSuccess();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        //下载config失败
                        setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_CONFIG_ERROR, 0);
                    }
                } else {
                    //下载config失败
                    setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_CONFIG_ERROR, 0);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //下载config失败
                setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_CONFIG_ERROR, 0);
            }
        });
    }

    private void setStateAndProcess(RouteRefreshCallback.State state, int process) {
        if (routeRefreshCallback != null && (mState != state || mProcess != process)) {
            mState = state;
            mProcess = process;
            BusProvider.getInstance().post(new BusProvider.BusEvent(Constants.BUS_EVENT_UPDATE_STATE_AND_PROCESS, null));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onsetStateAndProcess(BusProvider.BusEvent event) {
        if (event.eventId == Constants.BUS_EVENT_UPDATE_STATE_AND_PROCESS) {
            routeRefreshCallback.onResult(mState, mProcess);
        }
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
        setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_ROUTES, 0);
        loadLocalConfig();
        loadLocalRoutes();
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
                        routes = GsonHelper.getInstance().gson.fromJson(routesString, new TypeToken<List<Route>>(){}.getType());
                        downloadFiles(routes);
                    } catch (IOException e) {
                        e.printStackTrace();
                        //下载route失败
                        setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_ROUTES_ERROR, 0);
                    }

                } else {
                    //下载route失败
                    setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_ROUTES_ERROR, 0);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //下载route失败
                setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_ROUTES_ERROR, 0);
            }
        });
    }

    /**
     * 拷贝事件
     */
    private void copyWWWStart() {
        // 开始拷贝www
        setStateAndProcess(RouteRefreshCallback.State.COPY_WWW, 0);
    }

    private void copyWWW() {
        // 正在拷贝www
        copyFileCount++;
        process = copyFileCount * 100 / resourceRoutes.size();
        if (process > 99) {
            process = 99;
        }
        if (shouldDownloadWWW) {
            process = process / 2;
        }
        setStateAndProcess(RouteRefreshCallback.State.COPY_WWW, process);
    }

    private void copyWWWSuccess() {
        // 拷贝www成功
        String cachePath = InternalCache.getInstance().wwwCachePath() + File.separator;
        try {
            copyAssetFile(Constants.PRESET_CONFIG_FILE_PATH, cachePath + Constants.DEFAULT_DISK_CONFIG_FILE_NAME);
//            copyAssetFile(Constants.PRESET_ROUTE_FILE_PATH, cachePath + Constants.DEFAULT_DISK_ROUTES_FILE_NAME);
            // 保存路由表到数据库中
            liteOrm.save(resourceRoutes);
            if (shouldDownloadWWW) {
                downloadRoute();
            } else {
                updateSuccess();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 拷贝www失败
            setStateAndProcess(RouteRefreshCallback.State.COPY_WWW_ERROR, 0);
        }
    }

    private void copyWWWError() {
        // 拷贝www失败
        setStateAndProcess(RouteRefreshCallback.State.COPY_WWW_ERROR, 0);
    }

    /**
     * 下载事件
     */
    private synchronized void downloadFileSuccess() {
        // 单个下载成功
        downloadFileCount++;
        int copyProcess = process;
        int downloadProcess = downloadFileCount * (100 - copyProcess) / routes.size();
        process = copyProcess + downloadProcess;
        if (process > 99) {
            process = 99;
        }
        setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_FILES, process);
//            LogUtil.v("已下载的文件数量： "+ getDownloadFileCount() +"  文件总数: "+routes.size());
        if (downloadFileCount == routes.size()) {
            // 所有下载成功
            saveRouteAndConfig();
        }
    }

    protected synchronized void downloadFileError() {
        // 下载失败
        setStateAndProcess(RouteRefreshCallback.State.DOWNLOAD_FILES_ERROR, 0);
    }

    /**
     * 下载文件
     */
    private void downloadFiles(List<Route> routes) {
//        // 为了保证www的完整性，必须在下载时把原来的删掉
//        deleteCachedRoutes();
//        deleteCachedConfig();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(remoteFolderUrl).build();
        final DownloadService downloadService = retrofit.create(DownloadService.class);

        // 智能并发调度控制器：设置[最大并发数]，和[等待队列]大小
        final SmartExecutor smallExecutor = new SmartExecutor();
        // 开发者均衡性能和业务场景，自己调整同一时段的最大并发数量
        smallExecutor.setCoreSize(4);
        // 开发者均衡性能和业务场景，自己调整最大排队线程数量
        smallExecutor.setQueueSize(routes.size());
        // 任务数量超出[最大并发数]后，自动进入[等待队列]，等待当前执行任务完成后按策略进入执行状态：先进先执行。
        smallExecutor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
        // 后续添加新任务数量超出[等待队列]大小时，执行过载策略：抛出异常
        smallExecutor.setOverloadPolicy(OverloadPolicy.ThrowExecption);

        for (final Route route : routes) {
            smallExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    boolean success = downloadFile(route, downloadService);
                    if (!success) {
                        smallExecutor.cancelWaitingTask(null);
                    }
                }
            });
        }
    }

    /**
     * 下载文件
     */
    private boolean downloadFile(final Route route, final DownloadService downloadService) {
        // 进度
        if (shouldDownload(route)) {
            // 需要下载
//            LogUtil.v("需要下载的文件地址： " + route.file);
            Call<ResponseBody> call = downloadService.downloadFile(route.file);
            try {
                ResponseBody responseBody = call.execute().body();
                if (responseBody != null) {
                    // 下载成功，保存
                    InternalCache.getInstance().saveCache(route, responseBody.bytes());
                    liteOrm.save(route);
                    downloadFileSuccess();
                } else {
                    // 下载失败
                    downloadFileError();
                    return false;
                }
            } catch (IOException e) {
                // 下载失败
                e.printStackTrace();
                downloadFileError();
                return false;
            }
        } else {
            // 不需要下载
            downloadFileSuccess();
        }
        return true;
    }

    /**
     * 保存路由和配置
     */
    private void saveRouteAndConfig() {
//        saveCachedRoutes(routesString);
        saveCachedConfig(configString);
        updateSuccess();
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
    private boolean shouldDownloadWWW(Config config) {
        if (hasMinVersion(config)) {
            // 满足最小版本要求
            if (isWWwFolderNeedsToBeInstalled()) {
                return config.release.compareTo(resourceConfig.release) > 0;
            } else {
                return config.release.compareTo(cacheConfig.release) > 0;
            }
        }
        return false;
    }

    /**
     * 更新成功
     */
    private void updateSuccess() {
        loadLocalRoutes();
        loadLocalConfig();
//        if (isWWwFolderNeedsToBeInstalled()) {
//            // 从asset加载
//            wwwPath = AssetCache.getInstance().wwwAssetsPath();
//        } else {
//            // 从data加载
//            wwwPath = "file://" + InternalCache.getInstance().wwwCachePath();
//        }
        // 成功，进APP
        setStateAndProcess(RouteRefreshCallback.State.UPDATE_FILES_SUCCESS, 100);
    }

    /**
     * 把www文件夹安装到外部存储
     */
    private void unzipAssetToData() {
        copyWWWStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 为了保证www的完整性，必须在拷贝时把原来的删掉
                    InternalCache.getInstance().clearWWW();
                    liteOrm.deleteAll(Route.class);
                    // 解压文件
                    String outputDirectory = InternalCache.getInstance().wwwCachePath();
                    FilesUtility.unZip(AppContext.getInstance(), Constants.DEFAULT_ASSET_ZIP_PATH, outputDirectory, true, new FilesUtility.UnZipCallback() {
                        @Override
                        public void onUnzipFile() {
                            copyWWW();
                        }
                    });
                    copyWWWSuccess();
                } catch (IOException e) {
                    e.printStackTrace();
                    copyWWWError();
                }
            }
        }).start();
    }

    /**
     * 拷贝本地www到外部www
     */
    private void copyAssetFile(String assetFilePath, String destinationFilePath) throws IOException {
        AssetManager assetManager = AppContext.getInstance().getResources().getAssets();
        InputStream in = assetManager.open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);
        byte[] buf = new byte[1024 * 1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }


}
