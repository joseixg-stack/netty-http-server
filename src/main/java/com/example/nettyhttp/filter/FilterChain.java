package com.example.nettyhttp.filter;

import java.util.List;

public class FilterChain implements Chain {
    private final List<GatewayFilter> filters;
    private final TerminalHandler terminalHandler;
    private int index;

    public FilterChain(List<GatewayFilter> filters, TerminalHandler terminalHandler) {
        this.filters = List.copyOf(filters);
        this.terminalHandler = terminalHandler;
    }

    public void filter(Request request, Response response) throws Exception {
        next(request, response);
    }

    @Override
    public void next(Request request, Response response) throws Exception {
        if (response.isCommitted()) {
            return;
        }

        if (index < filters.size()) {
            GatewayFilter nextFilter = filters.get(index++);
            nextFilter.filter(request, response, this);
            return;
        }

        terminalHandler.handle(request, response);
    }

    @FunctionalInterface
    public interface TerminalHandler {
        void handle(Request request, Response response) throws Exception;
    }
}
