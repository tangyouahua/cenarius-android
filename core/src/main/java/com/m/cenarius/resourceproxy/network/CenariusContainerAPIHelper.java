package com.m.cenarius.resourceproxy.network;

import android.text.TextUtils;

import com.m.cenarius.Constants;

import java.util.ArrayList;
import java.util.List;

//import okhttp3.Request;
//import okhttp3.Response;


public class CenariusContainerAPIHelper {

    private static List<CenariusContainerAPI> mAPIS = new ArrayList<>();

    public static void registerAPI(CenariusContainerAPI api) {
        if (null != api) {
            mAPIS.add(api);
        }
    }

    public static void registerAPIs(List<CenariusContainerAPI> apis) {
        if (null != apis && !apis.isEmpty()) {
            mAPIS.addAll(apis);
        }
    }

//    public static Response handle(Request request) {
//        for (CenariusContainerAPI api : mAPIS) {
//            String requestUrl = request.url().toString();
//            int fragment = requestUrl.lastIndexOf('#');
//            if (fragment > 0) {
//                requestUrl = requestUrl.substring(0, fragment);
//            }
//
//            int query = requestUrl.lastIndexOf('?');
//            if (query > 0) {
//                requestUrl = requestUrl.substring(0, query);
//            }
//            if (!TextUtils.equals(Constants.CONTAINER_API_BASE + api.getPath(), requestUrl)) {
//                continue;
//            }
//            Response response = api.call(request);
//            if (null != response) {
//                return response;
//            }
//        }
//        return null;
//    }

}
