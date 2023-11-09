package com.jerry.pilipala.domain.user.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jerry.pilipala.domain.user.entity.mongo.Permission;
import com.jerry.pilipala.domain.user.entity.mongo.Role;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.infrastructure.enums.redis.UserCacheKeyEnum;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
                .get(UserCacheKeyEnum.StringKey.USER_CACHE_KEY.concat((String) loginId)), User.class);
        if (Objects.isNull(user)) {
            user = mongoTemplate.findById(new ObjectId((String) loginId), User.class);
            if (Objects.isNull(user)) {
                return Lists.newArrayList();
            }
            redisTemplate.opsForValue().set(UserCacheKeyEnum.StringKey.USER_CACHE_KEY.concat((String) loginId),
                    user, (long) (Math.random() * 3 + 2), TimeUnit.MINUTES);
        }
        String roleId = user.getRoleId();
        Role role = null;
        if (StringUtils.isNotBlank(roleId)) {
            role = mapper.convertValue(
                    redisTemplate.opsForHash().get(UserCacheKeyEnum.HashKey.ROLE_CACHE_KEY, roleId),
                    Role.class);
            if (Objects.isNull(role)) {
                role = mongoTemplate.findById(new ObjectId(roleId), Role.class);
                if (Objects.isNull(role)) {
                    return Lists.newArrayList();
                }
                redisTemplate.opsForHash().put(UserCacheKeyEnum.HashKey.ROLE_CACHE_KEY, roleId, role);
            }
        }
        if (Objects.isNull(role)) {
            role = new Role();
        }

        Collection<String> permissionIdList = role.getPermissionIds();
        List<String> permissions = redisTemplate.opsForHash()
                .multiGet(UserCacheKeyEnum.HashKey.PERMISSION_CACHE_KEY,
                        Arrays.asList(permissionIdList.toArray()))
                .stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
        if (permissions.isEmpty() || permissions.size() != permissionIdList.size()) {
            List<ObjectId> permissionIds = role.getPermissionIds().stream().map(ObjectId::new).toList();
            List<Permission> permissionList = mongoTemplate.find(
                            new Query(Criteria.where("_id").in(permissionIds)), Permission.class)
                    .stream()
                    .toList();
            Map<String, String> permissionMap = permissionList.stream()
                    .collect(Collectors.toMap(p -> p.getId().toString(), Permission::getValue));
            redisTemplate.opsForHash().putAll(UserCacheKeyEnum.HashKey.PERMISSION_CACHE_KEY, permissionMap);

            permissions = permissionMap.values().stream().toList();
        }

        return permissions;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = mapper.convertValue(redisTemplate
                .opsForValue()
                .get(UserCacheKeyEnum.StringKey.USER_CACHE_KEY.concat((String) loginId)), User.class);
        if (Objects.isNull(user)) {
            user = mongoTemplate.findById(new ObjectId((String) loginId), User.class);
            if (Objects.isNull(user)) {
                return Lists.newArrayList();
            }
            redisTemplate.opsForValue().set(UserCacheKeyEnum.StringKey.USER_CACHE_KEY.concat((String) loginId),
                    user, (long) (Math.random() * 3 + 2), TimeUnit.MINUTES);
        }
        String roleId = user.getRoleId();
        Role role = mapper.convertValue(
                redisTemplate.opsForHash().get(UserCacheKeyEnum.HashKey.ROLE_CACHE_KEY, roleId),
                Role.class);
        if (Objects.isNull(role)) {
            role = mongoTemplate.findById(new ObjectId(roleId), Role.class);
            if (Objects.isNull(role)) {
                return Lists.newArrayList();
            }
            redisTemplate.opsForHash().put(UserCacheKeyEnum.HashKey.ROLE_CACHE_KEY, roleId, role);
        }
        return Collections.singletonList(role.getName());
    }
}
