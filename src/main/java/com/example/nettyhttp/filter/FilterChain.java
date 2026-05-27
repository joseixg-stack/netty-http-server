package com.example.nettyhttp.filter;

public interface FilterChain {
    void doFilter(HttpRequestContext context) throws Exception;
}

