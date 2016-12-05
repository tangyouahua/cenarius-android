package com.m.cenarius.resourceproxy.network;

import com.m.cenarius.Cenarius;

import java.io.IOException;

//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;


public class NetworkImpl implements INetwork {

    public static final String TAG = NetworkImpl.class.getSimpleName();

//    OkHttpClient mOkHttpClient;
//
//    public NetworkImpl() {
//        mOkHttpClient = Cenarius.getOkHttpClient();
//    }
//
//    @Override
//    public Response handle(Request request) throws IOException {
//        try {
//            Response response = CenariusContainerAPIHelper.handle(request);
//            if (null == response) {
//                response = mOkHttpClient.newCall(request).execute();
//            }
//            return response;
//        } catch (Exception e) {
//            throw new IOException(e);
//        }
//    }

}
