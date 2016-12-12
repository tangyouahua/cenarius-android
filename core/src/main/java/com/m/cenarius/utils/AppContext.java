package com.m.cenarius.utils;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

/**
 * 用来存储app全局context实例
 */
public class AppContext extends ContextWrapper{

    private static AppContext sInstance;

    public static void init(Application application) {
        if (null == application) {
            return;
        }
        sInstance = new AppContext(application.getApplicationContext());
    }

    public static AppContext getInstance() {
        if (null == sInstance) {
            throw new IllegalStateException("AppContext must be initialized first!");
        }
        return sInstance;
    }

    public AppContext(Context base) {
        super(base);
    }

}
