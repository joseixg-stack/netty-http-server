package com.example.nettyhttp.config;

public record Route(String id, String pathPrefix, String target, boolean stripPrefix) {
}

