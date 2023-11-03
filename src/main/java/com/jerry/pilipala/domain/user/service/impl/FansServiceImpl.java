package com.jerry.pilipala.domain.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.domain.user.entity.mongo.Fans;
import com.jerry.pilipala.domain.user.service.FansService;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.infrastructure.enums.UserRelationEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FansServiceImpl implements FansService {
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private final UserService userService;

    private final UserEntityRepository userEntityRepository;

    public FansServiceImpl(MongoTemplate mongoTemplate,
                           RedisTemplate<String, Object> redisTemplate,
                           UserService userService,
                           UserEntityRepository userEntityRepository) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public UserVO put(String upUid, Integer relation) {
        String myUid = (String) StpUtil.getLoginId();
        if (upUid.equals(myUid)) {
            throw new BusinessException("无须关注自己", StandardResponse.ERROR);
        }

//        Update update = new Update();
//        update.set("fansId", myUid).set("upId", upUid).bitwise("deleted").xor(1);
//        mongoTemplate.upsert(new Query(Criteria.where("fansId").is(myUid).and("upId").is(upUid)),
//                update, Fans.class);
        UserEntity mySelf = userEntityRepository.findById(myUid).orElse(null);
        if (Objects.isNull(mySelf)) {
            return null;
        }
        UserEntity upEntity = userEntityRepository.findById(upUid).orElse(null);
        UserRelationEnum relationEnum = UserRelationEnum.parse(relation);
        switch (relationEnum) {
            case FOLLOW -> {
                if (!mySelf.getFollowUps().contains(upEntity)) {
                    mySelf.getFollowUps().add(upEntity);
                }
            }
            case UNFOLLOW -> mySelf.getFollowUps().remove(upEntity);
        }

        userEntityRepository.save(mySelf);

        return userService.userVO(myUid);
    }

    @Override
    public List<UserVO> idles() {
        String myUid = (String) StpUtil.getLoginId();
        List<Fans> fansList = mongoTemplate.find(
                new Query(Criteria.where("fansId").is(myUid)
                        .and("deleted").is(0x01)),
                Fans.class);

        Set<String> uidSet = fansList.stream().map(Fans::getUpId).collect(Collectors.toSet());
        return userService.userVoList(uidSet);
    }
}
