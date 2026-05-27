package com.example.nettyhttp.filter;

import java.util.List;

public class DefaultFilterChain implements FilterChain {
    private final List<Filter> filters;
    private final Runnable terminalHandler;
    private int index;

    public DefaultFilterChain(List<Filter> filters, Runnable terminalHandler) {
        this.filters = List.copyOf(filters);
        this.terminalHandler = terminalHandler;
    }

    @Override
    public void doFilter(HttpRequestContext context) throws Exception {
        if (index < filters.size()) {
            Filter nextFilter = filters.get(index++);
            nextFilter.doFilter(context, this);
            return;
        }

        terminalHandler.run();
    }
}

