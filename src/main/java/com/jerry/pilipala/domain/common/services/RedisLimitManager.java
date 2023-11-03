package com.jerry.pilipala.domain.common.services;

import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import com.jerry.pilipala.infrastructure.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class RedisLimitManager implements LimitManager {
    private final HttpServletRequest request;

    private final RedisTemplate<String, Object> stringRedisTemplate;
    private final DefaultRedisScript<Long> redisScript;

    public RedisLimitManager(HttpServletRequest request,
                             RedisTemplate<String, Object> stringRedisTemplate,
                             DefaultRedisScript<Long> redisScript) {
        this.request = request;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisScript = redisScript;
    }

    @Override
    public boolean tryAccess(RateLimiter limiter) {
        String key = limiter.key();
        if (Strings.isBlank(key)) {
            throw new BusinessException("redis limiter key cannot be null", StandardResponse.ERROR);
        }

        if (limiter.limitType() == LimitType.IP) {
            String ip = RequestUtil.getIpAddress(request);
            key += "-" + ip;
        }
        List<String> keys = new ArrayList<>();
        keys.add(key);

        int seconds = limiter.seconds();
        int limitCount = limiter.count();
        Long count;
        try {
            count = stringRedisTemplate.execute(redisScript, keys, limitCount, seconds);
        } catch (Exception e) {
            log.error("failed to execute redis script", e);
            throw new BusinessException("服务不可用，请稍后重试", StandardResponse.ERROR);
        }

        log.debug("Access try count is {} for key={}", count, key);

        return Objects.nonNull(count) && count != 0L && count <= limitCount;
    }
}
