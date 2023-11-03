package com.jerry.pilipala.domain.user.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.collect.Lists;
import com.jerry.pilipala.application.bo.UserInfoBO;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.domain.common.services.SmsService;
import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.domain.user.entity.mongo.Fans;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.infrastructure.utils.CaptchaUtil;
import com.jerry.pilipala.infrastructure.utils.UserGroupCount;
import com.jerry.pilipala.application.vo.user.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final HttpServletRequest request;

    private final SmsService smsService;

    public UserServiceImpl(RedisTemplate<String, Object> redisTemplate,
                           MongoTemplate mongoTemplate,
                           HttpServletRequest request,
                           SmsService smsService) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
        this.request = request;
        this.smsService = smsService;
    }

    @Override
    public UserVO login(LoginDTO loginDTO) {
        String loginCodeKey = "login-%s".formatted(loginDTO.getTel());
        String code = (String) redisTemplate.opsForValue().get(loginCodeKey);
        if (StringUtils.isBlank(code) || !code.equals(loginDTO.getVerifyCode())) {
            throw new BusinessException("验证码错误", StandardResponse.ERROR);
        }
        redisTemplate.delete(loginCodeKey);
        // 密文存储手机号
        String encodeTel = Base64.getEncoder().encodeToString(loginDTO.getTel().getBytes());
        Query query = new Query(Criteria.where("tel").is(encodeTel));
        User user = mongoTemplate.findOne(query, User.class);
        if (Objects.isNull(user)) {
            String end = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            user = new User()
                    .setTel(encodeTel)
                    .setNickname("user_".concat(end))
                    .setPermissions(Collections.emptyList());
            user = mongoTemplate.save(user);
        }

        // 登录成功
        StpUtil.login(user.getUid().toString());


        UserInfoBO userInfoBO = new UserInfoBO()
                .setUid(user.getUid().toString())
                .setPermissions(user.getPermissions());
        StpUtil.getSession().set("user-info", userInfoBO);

        return userVO(user.getUid().toString());
    }

    @Override
    public String code(String tel) {
        String loginCodeKey = "login-%s".formatted(tel);
        String code = (String) redisTemplate.opsForValue().get(loginCodeKey);
        if (StringUtils.isNotBlank(code)) {
            return code;
        }
        // redis 不存在，生成一个新的
        code = CaptchaUtil.generatorCaptchaNumberByLength(6);
        redisTemplate.opsForValue().set(loginCodeKey, code, 60, TimeUnit.SECONDS);

        smsService.sendLoginCode(tel, code);
        // todo send message
        return code;
    }

    @Component
    public class StpInterfaceImpl implements StpInterface {

        @Override
        public List<String> getPermissionList(Object loginId, String loginType) {
            User user = mongoTemplate.findOne(
                    new Query(Criteria.where("_id")
                            .is(new ObjectId(String.valueOf(loginId)))),
                    User.class);
            return Objects.isNull(user) ? Lists.newArrayList() : user.getPermissions();
        }

        @Override
        public List<String> getRoleList(Object loginId, String loginType) {
            return null;
        }
    }

    @Override
    public UserVO userVO(String uid) {
        if (StringUtils.isBlank(uid)) {
            uid = StpUtil.getLoginId(null);
            if (Objects.isNull(uid)) {
                throw new BusinessException("用户不存在", StandardResponse.ERROR);
            }
        }
        User user = mongoTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(uid))),
                User.class
        );
        if (Objects.isNull(user)) {
            throw new BusinessException("用户不存在", StandardResponse.ERROR);
        }

        long fansCount = mongoTemplate.count(new Query(Criteria.where("upId").is(uid)), Fans.class);
        long followCount = mongoTemplate.count(new Query(Criteria.where("fansId").is(uid)), Fans.class);

        UserVO userVO = new UserVO();
        userVO.setFansCount(fansCount)
                .setFollowCount(followCount)
                .setUid(user.getUid().toString())
                .setAvatar(user.getAvatar())
                .setNickName(user.getNickname());
        return userVO;
    }

    @Override
    public List<UserVO> userVoList(Set<String> uidSet) {
        List<User> userList = mongoTemplate.find(
                new Query(Criteria.where("_id").in(uidSet)),
                User.class);

        // 聚合查询粉丝数量
        Aggregation fansAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("upId").in(uidSet).and("deleted").is(0x01)),
                Aggregation.group("upId").count().as("count"),
                Aggregation.project().and("count").as("count")
                        .and("_id").as("uid")
        );

        List<UserGroupCount> fansCountList = mongoTemplate
                .aggregate(fansAggregation, "fans", UserGroupCount.class)
                .getMappedResults();
        Map<String, Long> fansCountMap = fansCountList
                .stream()
                .collect(Collectors.toMap(UserGroupCount::getUid, UserGroupCount::getCount));

        // 聚合查询关注数量
        Aggregation followAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("fansId").in(uidSet).and("deleted").is(0x01)),
                Aggregation.group("fansId").count().as("count"),
                Aggregation.project().and("count").as("count")
                        .and("_id").as("uid")
        );
        List<UserGroupCount> followCountList = mongoTemplate
                .aggregate(followAggregation, "fans", UserGroupCount.class)
                .getMappedResults();
        Map<String, Long> followCountMap = followCountList
                .stream()
                .collect(Collectors.toMap(UserGroupCount::getUid, UserGroupCount::getCount));

        // 构建 user 视图模型
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            userVO.setUid(user.getUid().toString())
                    .setAvatar(user.getAvatar())
                    .setNickName(user.getNickname())
                    .setIntro(user.getIntro());
            Long fansCount = fansCountMap.getOrDefault(user.getUid().toString(), 0L);
            Long followCount = followCountMap.getOrDefault(user.getUid().toString(), 0L);
            userVO.setFansCount(fansCount)
                    .setFollowCount(followCount);
            return userVO;
        }).toList();
        return userVOList;
    }
}
