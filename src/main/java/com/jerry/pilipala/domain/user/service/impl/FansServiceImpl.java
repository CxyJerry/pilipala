package com.jerry.pilipala.domain.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.domain.user.service.FansService;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.enums.UserRelationEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class FansServiceImpl implements FansService {
    private final UserService userService;

    private final UserEntityRepository userEntityRepository;

    public FansServiceImpl(UserService userService,
                           UserEntityRepository userEntityRepository) {
        this.userService = userService;
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public UserVO put(String upUid, Integer relation) {
        String myUid = (String) StpUtil.getLoginId();
        if (upUid.equals(myUid)) {
            throw new BusinessException("无须关注自己", StandardResponse.ERROR);
        }

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

        return userService.userVO(myUid, true);
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
}