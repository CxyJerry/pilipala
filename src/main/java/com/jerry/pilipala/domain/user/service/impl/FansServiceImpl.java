package com.jerry.pilipala.domain.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.vo.user.DynamicVO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.application.vo.vod.VodVO;
import com.jerry.pilipala.domain.user.entity.mongo.Dynamic;
import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.domain.user.service.FansService;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FansServiceImpl implements FansService {
    private final UserService userService;
    private final VodService vodService;

    private final UserEntityRepository userEntityRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    public FansServiceImpl(UserService userService,
                           VodService vodService,
                           UserEntityRepository userEntityRepository,
                           RedisTemplate<String, Object> redisTemplate,
                           MongoTemplate mongoTemplate) {
        this.userService = userService;
        this.vodService = vodService;
        this.userEntityRepository = userEntityRepository;
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void put(String uid, String upUid) {
        if (upUid.equals(uid)) {
            throw new BusinessException("无须关注自己", StandardResponse.ERROR);
        }

        UserEntity mySelf = userEntityRepository.findById(uid).orElse(null);
        if (Objects.isNull(mySelf)) {
            throw BusinessException.businessError("用户不存在");
        }
        Set<String> followSet = mySelf.getFollowUps()
                .stream()
                .map(UserEntity::getUid)
                .collect(Collectors.toSet());
        UserEntity upEntity = userEntityRepository.findById(upUid).orElse(null);
        if (Objects.isNull(upEntity)) {
            throw BusinessException.businessError("用户不存在");
        }

        if (followSet.contains(upUid)) {
            userEntityRepository.removeFollowUpRelation(uid, upUid);
        } else {
            mySelf.getFollowUps().add(upEntity);
            userEntityRepository.save(mySelf);
        }
    }

    @Override
    public List<UserVO> idles() {
        String myUid = (String) StpUtil.getLoginId();

        UserEntity userEntity = userEntityRepository.findById(myUid).orElse(null);
        if (Objects.isNull(userEntity)) {
            throw BusinessException.businessError("用户不存在");
        }

        List<UserEntity> followUps = userEntity.getFollowUps();

        List<String> uidSet = followUps.stream().map(UserEntity::getUid).toList();

        return userService.userVoList(uidSet);
    }

    @Override
    public Page<DynamicVO> dynamic(String authorUid, Integer pageNo, Integer pageSize) {
        Page<DynamicVO> page = new Page<>();
        page.setPageNo(pageNo)
                .setPageSize(pageSize);
        String uid = (String) StpUtil.getLoginId();
        String key = VodCacheKeyEnum.SetKey.OFTEN_INTERACTIVE_SET.concat(uid);
        List<String> uidList = new ArrayList<>();
        if (StringUtils.isBlank(authorUid)) {
            Set<Object> uidSet = redisTemplate.opsForZSet().range(key, 0, 20);
            if (Objects.isNull(uidSet)) {
                return page;
            }
            uidList = uidSet.stream()
                    .map(Object::toString).toList();
        } else {
            uidList.add(authorUid);
        }

        Query query = new Query(Criteria.where("_id").in(uidList));
        long total = mongoTemplate.count(query, Dynamic.class);

        query.with(Sort.by(Sort.Direction.DESC, "ctime"))
                .skip((long) Math.max(pageNo - 1, 0) * pageSize)
                .limit(pageSize);
        List<Dynamic> dynamicList = mongoTemplate.find(
                query,
                Dynamic.class
        );
        List<DynamicVO> dynamicVOList = dynamicList.stream().map(this::buildDynamic).toList();

        page.setTotal(total)
                .setPage(dynamicVOList);
        return page;
    }

    public DynamicVO buildDynamic(Dynamic dynamic) {
        DynamicVO dynamicVO = new DynamicVO();
        String uid = dynamic.getUid();
        UserVO userVO = userService.userVO(uid, false);
        Long cid = dynamic.getCid();
        VodInfo vodInfo = mongoTemplate.findById(cid, VodInfo.class);
        VodVO vodVO = vodService
                .batchBuildVodVOWithoutQuality(
                        Collections.singletonList(vodInfo), true)
                .get(0);
        return dynamicVO.setUserVO(userVO)
                .setVodVO(vodVO)
                .setCtime(dynamic.getCtime());
    }
}
