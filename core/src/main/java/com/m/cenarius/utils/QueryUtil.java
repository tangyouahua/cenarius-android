package com.m.cenarius.utils;

import android.net.Uri;

import org.xutils.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * query 的 map 与 string 互转。注意 map 的 value 是 array
 */

public class QueryUtil {

    public static String mapToString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            String value = map.get(key);
            try {
                stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                stringBuilder.append("=");
                stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * 将 query 以字典形式返回。
     */
    public static Map<String, List<String>> queryMap(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        Map<String, List<String>> map = new HashMap<>();

        String[] nameValuePairs = query.split("&");
        for (String nameValuePair : nameValuePairs) {
            String[] nameValue = nameValuePair.split("=");
            if (nameValue.length == 2) {
                try {
                    map = addItemToMap(map, URLDecoder.decode(nameValue[1], "UTF-8"), URLDecoder.decode(nameValue[0], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("This method requires UTF-8 encoding support", e);
                }
            }
        }

        return map;
    }

    /**
     * 在字典以关键字添加一个元素。
     *
     * @param item 待添加的元素
     * @param key  关键字
     */
    public static Map<String, List<String>> addItemToMap(Map<String, List<String>> map, String item, String key) {
        List<String> obj = map.get(key);
        List<String> array = new ArrayList<>();
        if (obj != null) {
            array.addAll(obj);
        }
        array.add(item);
        map.put(key, array);
        return map;
    }

    public static String itemForKey(Map<String, List<String>> map, String key) {
        List<String> array = map.get(key);
        if (array == null) {
            return null;
        }
        return array.get(0);
    }

    /**
     * 从 url 中取出 query
     */
    public static String queryFromUrl(String url) {
        return Uri.parse(url).getQuery();
    }

    /**
     * 从 url 中取出 ? 之前的路径
     */
    public static String baseUrlFromUrl(String url) {
        if (url.contains("?")) {
            //如果请求的URL中带有"?",则说明此请求中有带参
            int index = url.indexOf("?");
            return url.substring(0, index);//截取到的URL
        }
        return url;
    }

    public static void addQueryForRequestParams(RequestParams requestParams, String url) {
        String query = queryFromUrl(url);
        Map<String, List<String>> map = queryMap(query);
        if (map != null) {
            for (String key : map.keySet()) {
                requestParams.addQueryStringParameter(key, itemForKey(map, key));
            }
        }
    }
}