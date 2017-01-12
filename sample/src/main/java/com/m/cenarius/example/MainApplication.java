package com.m.cenarius.example;

import android.app.Application;

import com.m.cenarius.Cenarius;
import com.m.cenarius.route.RouteManager;
import com.m.cenarius.utils.LogUtils;

import java.util.ArrayList;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化cenarius
        Cenarius.initialize(this);
        Cenarius.setDebug(BuildConfig.DEBUG);
        Cenarius.setLogin("https://uim-test.infinitus.com.cn/oauth20/accessToken", "gbss-bupm", "rfGd23Yhjd92JkpWe");
        ArrayList<String> whiteList = new ArrayList<>();
        whiteList.add("cordova");
        Cenarius.setRoutesWhiteList(whiteList);
//        Cenarius.setDevelopModeEnable(true);//开启调试模式，会禁用路由表，从SD卡读取。
        // 设置并刷新route
        RouteManager.getInstance().setRemoteFolderUrl("http://172.20.70.80/www");
//        RouteManager.getInstance().setRemoteFolderUrl("http://10.86.21.64:9080/h5/www");
        RouteManager.getInstance().refreshRoute(new RouteManager.RouteRefreshCallback() {
            @Override
            public void onResult(State state, int process) {
                LogUtils.i("process",String.valueOf(process));
                if (state == State.UPDATE_FILES_SUCCESS){
                    LogUtils.i("更新","成功");
                }
            }
        });
//        // 设置需要代理的资源
//        ResourceProxy.getInstance().addProxyHosts(PROXY_HOSTS);
//        // 设置local api
//        CenariusContainerAPIHelper.registerAPIs(FrodoContainerAPIs.sAPIs);
//        // 设置自定义的OkHttpClient
//        Cenarius.setOkHttpClient(new OkHttpClient().newBuilder()
//                .retryOnConnectionFailure(true)
//                .addNetworkInterceptor(new AuthInterceptor())
//                .build());
//        Cenarius.setHostUserAgent(" Cenarius/1.2.x ");

    }

}
