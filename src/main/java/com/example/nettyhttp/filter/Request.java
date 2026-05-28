package com.example.nettyhttp.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

public class Request {
    private final ChannelHandlerContext channelContext;
    private final FullHttpRequest rawRequest;
    private final String path;

    public Request(ChannelHandlerContext channelContext, FullHttpRequest rawRequest) {
        this.channelContext = channelContext;
        this.rawRequest = rawRequest;
        this.path = new QueryStringDecoder(rawRequest.uri()).path();
    }

    public ChannelHandlerContext channelContext() {
        return channelContext;
    }

    public FullHttpRequest rawRequest() {
        return rawRequest;
    }

    public String path() {
        return path;
    }
}

