package com.jerry.pilipala.domain.user.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.infrastructure.enums.redis.UserCacheKeyEnum;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class StpInterfaceImpl implements StpInterface {
    private final UserEntityRepository userEntityRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper mapper;

    public StpInterfaceImpl(UserEntityRepository userEntityRepository,
                            RedisTemplate<String, Object> redisTemplate,
                            MongoTemplate mongoTemplate,
                            ObjectMapper mapper) {
        this.userEntityRepository = userEntityRepository;
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
        this.mapper = mapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        User user = mapper.convertValue(redisTemplate
                .opsForValue()
                .get(UserCacheKeyEnum.StringKey.USER_ENTITY_KEY.concat((String) loginId)), User.class);
        if (Objects.isNull(user)) {
            user = mongoTemplate.findById(new ObjectId((String) loginId), User.class);
            if (Objects.isNull(user)) {
                return Lists.newArrayList();
            }
            redisTemplate.opsForValue().set(UserCacheKeyEnum.StringKey.USER_ENTITY_KEY.concat((String) loginId),
                    user, (long) (Math.random() * 3 + 2), TimeUnit.MINUTES);
        }
        return user.getPermission();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return null;
    }
}
