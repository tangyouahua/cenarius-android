package com.m.cenarius.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;

public class GsonHelper {

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
