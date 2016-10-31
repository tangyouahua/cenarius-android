package com.m.cenarius.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

public class GsonHelper {

    private static final String KEY_DATA = "data";
    private static Gson sInstance;

    public static Gson getInstance() {
        if (null == sInstance) {
            synchronized (GsonHelper.class) {
                if (null == sInstance) {
                    sInstance = new GsonBuilder().serializeNulls()
                            .create();
                }
            }
        }
        return sInstance;
    }

    public static HashMap getDataMap(String url, String path) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Uri uri = Uri.parse(url);
        if (TextUtils.equals(uri.getPath(), path)) {
            String dataJson = uri.getQueryParameter(KEY_DATA);
            HashMap dataMap = GsonHelper.getInstance().fromJson(dataJson, HashMap.class);
            if (dataMap != null) {
                return dataMap;
            }
        }
        return null;
    }

}
