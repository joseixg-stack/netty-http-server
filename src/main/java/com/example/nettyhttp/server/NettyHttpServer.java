package com.example.nettyhttp.server;

import com.example.nettyhttp.config.GatewayConfig;
import com.example.nettyhttp.filter.FilterChain;
import com.example.nettyhttp.filter.GatewayFilter;
import com.example.nettyhttp.filter.PathLoggingFilter;
import com.example.nettyhttp.filter.Request;
import com.example.nettyhttp.filter.Response;
import com.example.nettyhttp.gateway.ProxyHandler;
import com.example.nettyhttp.gateway.RouteMatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.List;

public class NettyHttpServer {
    public static void main(String[] args) throws InterruptedException {
        GatewayConfig config = GatewayConfig.load();
        List<GatewayFilter> filters = List.of(new PathLoggingFilter());
        FilterChain.TerminalHandler terminalHandler = new ProxyHandler(new RouteMatcher(config.routes()));
        new NettyHttpServer().start(config.port(), filters, terminalHandler);
    }

    public void start(int port, List<GatewayFilter> filters, FilterChain.TerminalHandler terminalHandler) throws InterruptedException {
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
                                    .addLast(new RequestHandler(filters, terminalHandler));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Netty HTTP server started on port " + port);
            System.out.println("Health check: http://localhost:" + port + "/healthz");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        private final List<GatewayFilter> filters;
        private final FilterChain.TerminalHandler terminalHandler;

        private RequestHandler(List<GatewayFilter> filters, FilterChain.TerminalHandler terminalHandler) {
            this.filters = List.copyOf(filters);
            this.terminalHandler = terminalHandler;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelContext, FullHttpRequest request) throws Exception {
            Request gatewayRequest = new Request(channelContext, request);
            Response gatewayResponse = new Response(channelContext);
            FilterChain chain = new FilterChain(filters, terminalHandler);
            chain.filter(gatewayRequest, gatewayResponse);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext channelContext, Throwable cause) {
            cause.printStackTrace();
            new Response(channelContext).text(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }
}
