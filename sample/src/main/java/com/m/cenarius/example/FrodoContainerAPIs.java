package com.m.cenarius.example;


import android.net.Uri;
import android.util.Log;

import com.m.cenarius.resourceproxy.network.CenariusContainerAPI;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FrodoContainerAPIs {

    public static List<CenariusContainerAPI> sAPIs = new ArrayList<>();
    static {
        sAPIs.add(new LocationAPI());
        sAPIs.add(new LogAPI());
    }

    static Response.Builder newResponseBuilder(Request request) {
        Response.Builder responseBuilder = new Response.Builder();
        responseBuilder.request(request);
        responseBuilder.code(200);
        responseBuilder.protocol(Protocol.HTTP_1_1);
        return responseBuilder;
    }

    static class LocationAPI implements CenariusContainerAPI {

        @Override
        public String getPath() {
            return "/geo";
        }

        @Override
        public Response call(Request request) {
            Response.Builder responseBuilder = newResponseBuilder(request);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("lat", "0.0");
                jsonObject.put("lng", "0.0");
                responseBuilder.body(ResponseBody.create(MediaType.parse(Constants.MIME_TYPE_JSON), jsonObject.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseBuilder.build();
        }
    }

    static class LogAPI implements CenariusContainerAPI {

        @Override
        public String getPath() {
            return "/log";
        }

        @Override
        public Response call(Request request) {
            Response.Builder responseBuilder = newResponseBuilder(request);
            String url = request.url().toString();
            Uri uri = Uri.parse(url);
            String event = uri.getQueryParameter("event");
            String label = uri.getQueryParameter("label");
            Log.i("Cenarius", "event: " + event + " ; label : " + label);
            responseBuilder.body(ResponseBody.create(MediaType.parse(Constants.MIME_TYPE_JSON), event));
            return responseBuilder.build();
        }
    }
}
