package com.jerry.pilipala.domain.common.services;


import com.jerry.pilipala.infrastructure.annotations.RateLimiter;

public interface LimitManager {

    boolean tryAccess(RateLimiter limiter);
}
