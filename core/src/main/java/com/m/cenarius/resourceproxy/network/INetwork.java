package com.m.cenarius.resourceproxy.network;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;


public interface INetwork {

    /**
     * handle api request, should be sync
     *
     * @param request
     * @return
     */
    Response handle(Request request) throws IOException;
}
