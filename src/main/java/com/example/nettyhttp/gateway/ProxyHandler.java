package com.example.nettyhttp.gateway;

import com.example.nettyhttp.config.Route;
import com.example.nettyhttp.filter.FilterChain;
import com.example.nettyhttp.filter.Request;
import com.example.nettyhttp.filter.Response;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ProxyHandler implements FilterChain.TerminalHandler {
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length"
    );

    private final RouteMatcher routeMatcher;
    private final HttpClient httpClient;

    public ProxyHandler(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        if ("/healthz".equals(request.path())) {
            response.text("OK");
            return;
        }

        Optional<Route> route = routeMatcher.match(request.path());
        if (route.isEmpty()) {
            response.text(HttpResponseStatus.NOT_FOUND, "No route matched: " + request.path());
            return;
        }

        HttpRequest upstreamRequest = buildUpstreamRequest(route.get(), request);
        HttpResponse<byte[]> upstreamResponse = httpClient.send(upstreamRequest, HttpResponse.BodyHandlers.ofByteArray());
        response.bytes(upstreamResponse.statusCode(), upstreamResponse.body(), upstreamResponse.headers().map());
    }

    private HttpRequest buildUpstreamRequest(Route route, Request request) {
        URI targetUri = URI.create(route.target());
        URI upstreamUri = targetUri.resolve(upstreamPath(route, request));
        HttpRequest.Builder builder = HttpRequest.newBuilder(upstreamUri)
                .timeout(Duration.ofSeconds(30))
                .method(request.method(), HttpRequest.BodyPublishers.ofByteArray(request.body()));

        request.headers().forEach(header -> {
            String name = header.getKey();
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                builder.header(name, header.getValue());
            }
        });

        return builder.build();
    }

    private String upstreamPath(Route route, Request request) {
        String path = request.path();
        if (route.stripPrefix()) {
            path = path.substring(route.pathPrefix().length());
            if (path.isBlank()) {
                path = "/";
            }
        }

        String query = request.query();
        return query == null || query.isBlank() ? path : path + "?" + query;
    }
}

