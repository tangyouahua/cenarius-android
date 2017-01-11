package com.m.cenarius.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;

import java.util.HashMap;

public class GsonHelper {

    private static GsonHelper instance;
    public Gson gson;

    public static GsonHelper getInstance() {
        if (null == instance) {
            synchronized (GsonHelper.class) {
                if (null == instance) {
                    instance = new GsonHelper();
                }
            }
        }
        return instance;
    }

    private GsonHelper() {
        gson = new Gson();
    }

    private static final String KEY_DATA = "data";

    public static HashMap getDataMap(String url, String path) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Uri uri = Uri.parse(url);
        if (TextUtils.equals(uri.getPath(), path)) {
            String dataJson = uri.getQueryParameter(KEY_DATA);
            HashMap dataMap = JSON.parseObject(dataJson, HashMap.class);
            if (dataMap != null) {
                return dataMap;
            }
        }
        return null;
    }

}
