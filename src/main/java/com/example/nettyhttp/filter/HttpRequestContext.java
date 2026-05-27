package com.example.nettyhttp.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

public class HttpRequestContext {
    private final ChannelHandlerContext channelContext;
    private final FullHttpRequest request;
    private final String path;

    public HttpRequestContext(ChannelHandlerContext channelContext, FullHttpRequest request) {
        this.channelContext = channelContext;
        this.request = request;
        this.path = new QueryStringDecoder(request.uri()).path();
    }

    public ChannelHandlerContext channelContext() {
        return channelContext;
    }

    public FullHttpRequest request() {
        return request;
    }

    public String path() {
        return path;
    }
}

