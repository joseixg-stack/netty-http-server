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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Response {
    private static final Set<String> SKIPPED_RESPONSE_HEADERS = Set.of(
            "connection",
            "content-length",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade"
    );

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
        bytes(status.code(), body.getBytes(StandardCharsets.UTF_8), Map.of(HttpHeaderNames.CONTENT_TYPE.toString(), List.of("text/plain; charset=UTF-8")));
    }

    public void bytes(int statusCode, byte[] bytes, Map<String, List<String>> headers) {
        if (committed) {
            return;
        }

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(statusCode),
                Unpooled.wrappedBuffer(bytes)
        );

        headers.forEach((name, values) -> {
            if (!SKIPPED_RESPONSE_HEADERS.contains(name.toLowerCase())) {
                response.headers().set(name, values);
            }
        });

        response.headers()
                .set(HttpHeaderNames.CONTENT_LENGTH, bytes.length)
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

        committed = true;
        channelContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
