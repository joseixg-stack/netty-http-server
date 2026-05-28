package com.example.nettyhttp.filter;

public class PathLoggingFilter implements GatewayFilter {
    @Override
    public void filter(Request request, Response response, Chain chain) throws Exception {
        System.out.println("Request path: " + request.path());
        chain.next(request, response);
    }
}
