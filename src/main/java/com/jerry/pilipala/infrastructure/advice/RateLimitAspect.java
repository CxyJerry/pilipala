package com.jerry.pilipala.infrastructure.advice;

import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.domain.common.services.LimitManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RateLimitAspect {
    private final LimitManager limitManager;

    public RateLimitAspect(LimitManager limitManager) {

        this.limitManager = limitManager;
    }

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        if (!limitManager.tryAccess(rateLimiter)) {
            throw new BusinessException(rateLimiter.message(), StandardResponse.ERROR);
        }
    }
}

