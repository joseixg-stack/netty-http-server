package com.example.nettyhttp.filter;

public interface Filter {
    void doFilter(HttpRequestContext context, FilterChain chain) throws Exception;
}

