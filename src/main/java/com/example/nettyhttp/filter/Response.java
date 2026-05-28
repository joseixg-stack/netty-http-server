package com.example.nettyhttp.filter;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;

public class Response {
    private final ChannelHandlerContext channelContext;
    private boolean committed;

    public Response(ChannelHandlerContext channelContext) {
        this.channelContext = channelContext;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void text(String body) {
        text(HttpResponseStatus.OK, body);
    }

    public void text(HttpResponseStatus status, String body) {
        if (committed) {
            return;
        }

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.wrappedBuffer(bytes)
        );

        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .set(HttpHeaderNames.CONTENT_LENGTH, bytes.length)
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

        committed = true;
        channelContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}

