package com.example.nettyhttp.filter;

public class PathLoggingFilter implements Filter {
    @Override
    public void doFilter(HttpRequestContext context, FilterChain chain) throws Exception {
        System.out.println("Request path: " + context.path());
        chain.doFilter(context);
    }
}

