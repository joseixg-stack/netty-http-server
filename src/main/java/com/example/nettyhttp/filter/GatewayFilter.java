package com.example.nettyhttp.filter;

public interface GatewayFilter {
    void filter(Request request, Response response, Chain chain) throws Exception;
}

