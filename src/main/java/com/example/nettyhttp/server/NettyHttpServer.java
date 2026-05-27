package com.example.nettyhttp.server;

import com.example.nettyhttp.filter.DefaultFilterChain;
import com.example.nettyhttp.filter.Filter;
import com.example.nettyhttp.filter.HttpRequestContext;
import com.example.nettyhttp.filter.PathLoggingFilter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class NettyHttpServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws InterruptedException {
        List<Filter> filters = List.of(new PathLoggingFilter());
        new NettyHttpServer().start(PORT, filters);
    }

    public void start(int port, List<Filter> filters) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(1024 * 1024))
                                    .addLast(new RequestHandler(filters));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Netty HTTP server started on port " + port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private final List<Filter> filters;

        private RequestHandler(List<Filter> filters) {
            this.filters = List.copyOf(filters);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelContext, FullHttpRequest request) throws Exception {
            HttpRequestContext requestContext = new HttpRequestContext(channelContext, request);
            DefaultFilterChain chain = new DefaultFilterChain(filters, () -> writeOk(channelContext));
            chain.doFilter(requestContext);
        }

        private void writeOk(ChannelHandlerContext channelContext) {
            byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(body)
            );

            response.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .set(HttpHeaderNames.CONTENT_LENGTH, body.length)
                    .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

            channelContext.writeAndFlush(response).addListener(future -> channelContext.close());
        }
    }
}

