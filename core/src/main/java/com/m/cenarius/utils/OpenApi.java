package com.m.cenarius.utils;

import android.content.Context;

import com.m.cenarius.Cenarius;
import com.m.cenarius.widget.LoginWidget;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OpenApi {

    /**
     * md5 签名
     */
    public static String md5Signature(Map<String, String> params, String secret) {
        String result = null;
        StringBuffer orgin = getBeforeSign(params, new StringBuffer(secret));
        if (orgin == null) {
            return null;
        }
        // secret last
        orgin.append(secret);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            result = byte2hex(md.digest(orgin.toString().getBytes("utf-8")));
        } catch (Exception e) {
            throw new java.lang.RuntimeException("md5 sign error !", e);
        }
        return result;
    }

    private static StringBuffer getBeforeSign(Map<String, String> params, StringBuffer orgin) {
        if (params == null) {
            return null;
        }

        Map<String, String> map = new TreeMap<>();
        map.putAll(params);
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            orgin.append(name).append(params.get(name));
        }
        return orgin;
    }

    private static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer();
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs.append("0").append(stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString().toUpperCase();
    }

    /**
     * 返回签名之后的 Query
     */
    public static String openApiQuery(String query, String body, Context context) {
        String parameterString = query;
        if (body != null) {
            if (parameterString != null) {
                parameterString = parameterString + "&" + body;
            } else {
                parameterString = body;
            }
        }
        // 多值合并
        Map<String, String> parameters = new HashMap();
        Map<String, List<String>> oldParameters = QueryUtil.queryMap(parameterString);
        if (oldParameters != null) {
            Iterator<String> iter = oldParameters.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                List<String> array = oldParameters.get(key);
                Collections.sort(array);
                String value = array.get(0);
                for (int i = 1; i < array.size(); i++) {
                    value = value + key + array.get(i);
                }
                parameters.put(key, value);
            }
        }
        // 加入系统级参数
        String token = LoginWidget.getAccessToken();
        String appKey = Cenarius.LoginAppKey;
        String appSecret =Cenarius.LoginAppSecret;
        String timestamp = Long.toString((new Date()).getTime());
        if (token == null || appKey == null || appSecret == null){
            return null;
        }
        parameters.put("access_token", token);
        parameters.put("app_key", appKey);
        parameters.put("timestamp", timestamp);

        // 签名
        String sign = md5Signature(parameters, appSecret);
        if (query != null && query.length() > 0){
            query = query + "&access_token=" + token + "&app_key=" + appKey + "&timestamp=" + timestamp + "&sign=" + sign;
        }
        else {
            query = "access_token=" + token + "&app_key=" + appKey + "&timestamp=" + timestamp + "&sign=" + sign;
        }

        return query;
    }

}
