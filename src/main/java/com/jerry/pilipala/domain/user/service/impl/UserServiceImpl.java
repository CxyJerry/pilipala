package com.jerry.pilipala.domain.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerry.pilipala.application.bo.UserInfoBO;
import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.domain.message.service.MessageService;
import com.jerry.pilipala.domain.message.service.SmsService;
import com.jerry.pilipala.domain.user.entity.mongo.Apply;
import com.jerry.pilipala.domain.user.entity.mongo.Permission;
import com.jerry.pilipala.domain.user.entity.mongo.Role;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.domain.user.service.PermissionService;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.enums.ApplyStatusEnum;
import com.jerry.pilipala.infrastructure.enums.redis.UserCacheKeyEnum;
import com.jerry.pilipala.infrastructure.utils.CaptchaUtil;
import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.infrastructure.utils.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final SmsService smsService;
    private final UserEntityRepository userEntityRepository;
    private final ObjectMapper mapper;
    private final MessageService messageService;
    private final HttpServletRequest request;


    public UserServiceImpl(RedisTemplate<String, Object> redisTemplate,
                           MongoTemplate mongoTemplate,
                           SmsService smsService,
                           UserEntityRepository userEntityRepository,
                           ObjectMapper mapper,
                           MessageService messageService,
                           HttpServletRequest request) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
        this.smsService = smsService;
        this.userEntityRepository = userEntityRepository;
        this.mapper = mapper;
        this.messageService = messageService;
        this.request = request;
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
        User user = mongoTemplate.findOne(
                new Query(Criteria.where("tel").is(encodeTel)), User.class);

        if (Objects.isNull(user)) {
            String uid = UUID.randomUUID().toString().replace("-", "");
            String end = uid.substring(0, 8);

            user = new User().setNickname("user_".concat(end))
                    .setTel(encodeTel)
                    .setIntro("");

            user = mongoTemplate.save(user);

            UserEntity userEntity = new UserEntity()
                    .setUid(user.getUid().toString())
                    .setTel(user.getTel());
            userEntityRepository.save(userEntity);
        }

        // 登录成功
        StpUtil.login(user.getUid().toString());

        // 推送站内信
        User finalUser = user;
//        CompletableFuture.runAsync(() -> {
        String msg = "欢迎您，亲爱的%s, 您的账号于 %s ，在 IP: %s 进行登录。"
                .formatted(finalUser.getNickname(),
                        DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"),
                        RequestUtil.getIpAddress(request));
        messageService.send("", finalUser.getUid().toString(), msg);
//        });

        String roleId = user.getRoleId();
        Role role;
        if (StringUtils.isBlank(roleId)) {
            role = new Role();
        } else {
            role = mongoTemplate.findById(new ObjectId(roleId), Role.class);
            if (Objects.isNull(role)) {
                role = new Role();
            }
        }

        UserInfoBO userInfoBO = new UserInfoBO()
                .setUid(user.getUid().toString())
                .setRoleId(roleId)
                .setPermissionIdList(role.getPermissionIds());
        StpUtil.getSession().set("user-info", userInfoBO);

        return userVO(user.getUid().toString(), false);
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
        int expireMinutes = 1;
        redisTemplate.opsForValue().set(loginCodeKey, code, expireMinutes * 60, TimeUnit.SECONDS);

        smsService.sendCode(tel, code, expireMinutes);
        // todo send message
        return code;
    }

    @Override
    public UserVO userVO(String uid, boolean forceQuery) {
        if (StringUtils.isBlank(uid)) {
            uid = StpUtil.getLoginId("");
            if (StringUtils.isBlank(uid)) {
                throw new BusinessException("用户不存在", StandardResponse.ERROR);
            }
        }
        UserVO userVO;
        if (!forceQuery) {
            // 如果缓存有，直接走缓存
            userVO = mapper.convertValue(redisTemplate.opsForHash()
                    .get(UserCacheKeyEnum.HashKey.USER_VO_HASH_KEY, uid), UserVO.class);
            if (Objects.nonNull(userVO)) {
                return userVO;
            }

        }

        User user = mongoTemplate.findById(new ObjectId(uid), User.class);

        if (Objects.isNull(user)) {
            throw new BusinessException("用户不存在", StandardResponse.ERROR);
        }

        int fansCount = userEntityRepository.countFollowersByUserId(uid);

        int upCount = userEntityRepository.getFollowedUsersCount(uid);

        userVO = new UserVO();
        userVO.setFansCount(fansCount)
                .setFollowCount(upCount)
                .setUid(user.getUid().toString())
                .setAvatar(user.getAvatar())
                .setNickName(user.getNickname());
        // 存入缓存
        redisTemplate.opsForHash()
                .put(UserCacheKeyEnum.HashKey.USER_VO_HASH_KEY, uid, userVO);

        return userVO;
    }

    @Override
    public List<UserVO> userVoList(Collection<String> uidSet) {
        Map<String, UserVO> userVoMap = redisTemplate.opsForHash()
                .multiGet(UserCacheKeyEnum.HashKey.USER_VO_HASH_KEY,
                        Arrays.asList(uidSet.toArray()))
                .stream()
                .filter(Objects::nonNull)
                .map(u -> mapper.convertValue(u, UserVO.class))
                .collect(Collectors.toMap(UserVO::getUid, u -> u));

        uidSet.forEach(uid -> {
            if (!userVoMap.containsKey(uid)) {
                UserVO userVO = userVO(uid, true);
                userVoMap.put(uid, userVO);
            }
        });
        return userVoMap.values().stream().toList();
    }

    @Override
    public Page<UserVO> page(Integer pageNo, Integer pageSize) {
        List<User> userList = mongoTemplate.find(
                new Query()
                        .skip((long) Math.max(0, pageNo - 1) * pageSize)
                        .limit(pageSize),
                User.class);
        List<UserVO> userVOList = userList
                .stream()
                .map(user -> userVO(user.getUid().toString(), false))
                .toList();
        long total = mongoTemplate.count(new Query(), User.class);
        return new Page<UserVO>()
                .setPageNo(pageNo)
                .setPageSize(pageSize)
                .setTotal(total)
                .setPage(userVOList);
    }
}
