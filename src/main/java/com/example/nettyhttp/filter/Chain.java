package com.example.nettyhttp.filter;

public interface Chain {
    void next(Request request, Response response) throws Exception;
}

