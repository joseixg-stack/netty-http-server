package com.example.nettyhttp.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Map;

public class Request {
    private final ChannelHandlerContext channelContext;
    private final FullHttpRequest rawRequest;
    private final String path;
    private final String query;

    public Request(ChannelHandlerContext channelContext, FullHttpRequest rawRequest) {
        this.channelContext = channelContext;
        this.rawRequest = rawRequest;
        QueryStringDecoder decoder = new QueryStringDecoder(rawRequest.uri());
        this.path = decoder.path();
        int queryStart = rawRequest.uri().indexOf('?');
        this.query = queryStart >= 0 ? rawRequest.uri().substring(queryStart + 1) : "";
    }

    public ChannelHandlerContext channelContext() {
        return channelContext;
    }

    public FullHttpRequest rawRequest() {
        return rawRequest;
    }

    public String method() {
        return rawRequest.method().name();
    }

    public String path() {
        return path;
    }

    public String query() {
        return query;
    }

    public byte[] body() {
        byte[] bytes = new byte[rawRequest.content().readableBytes()];
        rawRequest.content().getBytes(rawRequest.content().readerIndex(), bytes);
        return bytes;
    }

    public Iterable<Map.Entry<String, String>> headers() {
        return rawRequest.headers();
    }

    public String contentType() {
        return rawRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }
}
