package com.m.cenarius.example;

import android.app.Application;
import android.widget.ImageView;

import com.m.cenarius.Cenarius;
import com.m.cenarius.resourceproxy.ResourceProxy;
import com.m.cenarius.resourceproxy.network.CenariusContainerAPIHelper;
import com.m.cenarius.route.RouteManager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

public class MainApplication extends Application {

//    static final List<String> PROXY_HOSTS = new ArrayList<>();
//    static {
//        PROXY_HOSTS.add("raw.githubusercontent.com");
//    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化cenarius
        Cenarius.initialize(this);
        Cenarius.setDebug(BuildConfig.DEBUG);
        Cenarius.setLogin("https://uim-test.infinitus.com.cn/oauth20/accessToken", "BUPM", "rfGd23Yhjd92JkpWe");
        ArrayList<String> whiteList = new ArrayList<>();
        whiteList.add("cordova");
        Cenarius.setRoutesWhiteList(whiteList);
//        Cenarius.setDevelopModeEnable(true);//开启调试模式，会禁用路由表，从SD卡读取。
        // 设置并刷新route
        RouteManager.getInstance().setRemoteFolderUrl("http://172.20.70.80/hybrid222");
        RouteManager.getInstance().refreshRoute(null);
//        // 设置需要代理的资源
//        ResourceProxy.getInstance().addProxyHosts(PROXY_HOSTS);
//        // 设置local api
//        CenariusContainerAPIHelper.registerAPIs(FrodoContainerAPIs.sAPIs);
//        // 设置自定义的OkHttpClient
        Cenarius.setOkHttpClient(new OkHttpClient().newBuilder()
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(new AuthInterceptor())
                .build());
//        Cenarius.setHostUserAgent(" Cenarius/1.2.x ");

    }

}
