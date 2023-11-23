package com.jerry.pilipala.infrastructure.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.config.LimitConfig;
import com.jerry.pilipala.infrastructure.utils.JsonHelper;
import com.jerry.pilipala.infrastructure.utils.RequestUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter implements Filter {
    private final LimitConfig limitConfig;
    private final JsonHelper jsonHelper;
    private final Cache<String, Integer> requestCountCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    public RateLimitFilter(LimitConfig limitConfig,
                           JsonHelper jsonHelper) {
        this.limitConfig = limitConfig;
        this.jsonHelper = jsonHelper;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String ip = RequestUtil.getIpAddress((HttpServletRequest) servletRequest);
        Integer count = requestCountCache.getIfPresent(ip);
        count = Objects.isNull(count) ? 0 : count;
        if (count > limitConfig.getIpMaxCount()) {
            CommonResponse<?> response = new CommonResponse<>
                    (StandardResponse.ERROR.getCode(), "您的访问频率太快了，请稍后重试", null);
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            httpServletResponse.setStatus(500);
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.setCharacterEncoding("utf-8");
            servletResponse.getWriter().write(jsonHelper.as(response));
        } else {
            requestCountCache.put(ip, count + 1);
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
