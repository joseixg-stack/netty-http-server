package com.example.nettyhttp.gateway;

import com.example.nettyhttp.config.Route;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RouteMatcher {
    private final List<Route> routes;

    public RouteMatcher(List<Route> routes) {
        this.routes = routes.stream()
                .sorted(Comparator.comparingInt((Route route) -> route.pathPrefix().length()).reversed())
                .toList();
    }

    public Optional<Route> match(String path) {
        return routes.stream()
                .filter(route -> matches(route.pathPrefix(), path))
                .findFirst();
    }

    private boolean matches(String prefix, String path) {
        return path.equals(prefix) || path.startsWith(prefix + "/") || "/".equals(prefix);
    }
}

