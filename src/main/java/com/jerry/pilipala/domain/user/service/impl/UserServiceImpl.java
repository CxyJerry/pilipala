package com.jerry.pilipala.domain.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.jerry.pilipala.application.bo.UserInfoBO;
import com.jerry.pilipala.application.dto.EmailLoginDTO;
import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.dto.UserUpdateDTO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.application.vo.vod.VodVO;
import com.jerry.pilipala.domain.common.template.MessageTrigger;
import com.jerry.pilipala.domain.message.service.SmsService;
import com.jerry.pilipala.domain.user.entity.mongo.Role;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.enums.message.TemplateNameEnum;
import com.jerry.pilipala.infrastructure.enums.redis.UserCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.utils.CaptchaUtil;
import com.jerry.pilipala.infrastructure.utils.JsonHelper;
import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.infrastructure.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final SmsService smsService;
    private final UserEntityRepository userEntityRepository;
    private final HttpServletRequest request;
    private final JsonHelper jsonHelper;
    private final MessageTrigger messageTrigger;
    private final VodService vodService;


    public UserServiceImpl(RedisTemplate<String, Object> redisTemplate,
                           MongoTemplate mongoTemplate,
                           SmsService smsService,
                           UserEntityRepository userEntityRepository,
                           HttpServletRequest request, JsonHelper jsonHelper,
                           MessageTrigger messageTrigger,
                           VodService vodService) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
        this.smsService = smsService;
        this.userEntityRepository = userEntityRepository;
        this.request = request;
        this.jsonHelper = jsonHelper;
        this.messageTrigger = messageTrigger;
        this.vodService = vodService;
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
        return loginLogic(user, encodeTel, "");
    }

    private User register(String tel, String email) {
        String uid = UUID.randomUUID().toString().replace("-", "");
        String end = uid.substring(0, 8);

        User user = new User().setNickname("user_".concat(end))
                .setTel(tel)
                .setEmail(email)
                .setIntro("");

        user = mongoTemplate.save(user);

        UserEntity userEntity = new UserEntity()
                .setUid(user.getUid().toString())
                .setTel(user.getTel())
                .setEmail(user.getEmail());
        userEntityRepository.save(userEntity);
        return user;
    }

    private UserVO loginLogic(User user, String tel, String email) {
        if (Objects.isNull(user)) {
            user = register(tel, email);
        }

        // 登录成功
        StpUtil.login(user.getUid().toString());

        // 推送站内信
        Map<String, String> variables = new HashMap<>();
        variables.put("user_name", user.getNickname());
        variables.put("time", DateUtil.format(LocalDateTime.now(), "yyyy-mm-dd hh:MM:ss"));
        variables.put("ip", RequestUtil.getIpAddress(request));
        variables.put("user_id", user.getUid().toString());
        messageTrigger.triggerSystemMessage(
                TemplateNameEnum.LOGIN_NOTIFY,
                user.getUid().toString(),
                variables
        );


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
    public void code(String tel) {
        String loginCodeKey = "login-%s".formatted(tel);
        String code = (String) redisTemplate.opsForValue().get(loginCodeKey);
        if (StringUtils.isNotBlank(code)) {
            throw BusinessException.businessError("验证码已发送");
        }
        // redis 不存在，生成一个新的
        code = CaptchaUtil.generatorCaptchaNumberByLength(6);
        int expireMinutes = 1;

        smsService.sendCode(tel, code, expireMinutes);

        redisTemplate.opsForValue().set(loginCodeKey, code, expireMinutes * 60, TimeUnit.SECONDS);
    }

    @Override
    public void emailCode(String email) {
        String emailCodeKey = "login-email-%s".formatted(email);
        String code = (String) redisTemplate.opsForValue().get(emailCodeKey);
        if (StringUtils.isNotBlank(code)) {
            throw BusinessException.businessError("验证码已发送");
        }
        // redis 不存在，生成一个新的
        code = CaptchaUtil.generatorCaptchaNumberByLength(6);
        int expireMinutes = 1;

        smsService.sendEmailCode(email, code, expireMinutes);
        redisTemplate.opsForValue().set(emailCodeKey, code, expireMinutes * 60, TimeUnit.SECONDS);
    }

    @Override
    public UserVO emailLogin(EmailLoginDTO loginDTO) {
        String loginCodeKey = "login-email-%s".formatted(loginDTO.getEmail());
        String code = (String) redisTemplate.opsForValue().get(loginCodeKey);
        if (StringUtils.isBlank(code) || !code.equals(loginDTO.getVerifyCode())) {
            throw new BusinessException("验证码错误", StandardResponse.ERROR);
        }
        redisTemplate.delete(loginCodeKey);
        // 密文存储手机号
        String encodeEmail = Base64.getEncoder().encodeToString(loginDTO.getEmail().getBytes());
        User user = mongoTemplate.findOne(
                new Query(Criteria.where("email").is(encodeEmail)), User.class);
        return loginLogic(user, "", encodeEmail);
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
            userVO = jsonHelper.convert(redisTemplate.opsForHash()
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
                .setAnnouncement(user.getAnnouncement())
                .setIntro(user.getIntro())
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
                .map(u -> jsonHelper.convert(u, UserVO.class))
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

    @Override
    public List<VodVO> collections(String uid, String setKey, Integer offset, Integer size) {
        if (StringUtils.isBlank(uid)) {
            uid = (String) StpUtil.getLoginId();
        }
        setKey = setKey.concat(uid);
        Long collectCount = redisTemplate.opsForZSet().size(setKey);
        if (Objects.isNull(collectCount)) {
            collectCount = 0L;
        }
        Set<Long> cidSet = Objects.requireNonNull(
                        redisTemplate.opsForZSet()
                                .reverseRange(setKey, offset, Math.min(offset + size, collectCount))
                )
                .stream()
                .filter(Objects::nonNull)
                .map(e -> Long.parseLong(e.toString()))
                .collect(Collectors.toSet());


        String finalSetKey = setKey;

        // 组装 视频稿件
        List<VodInfo> vodInfoList = mongoTemplate.find(
                new Query(Criteria.where("_id").in(cidSet)),
                VodInfo.class
        );
        return vodService.batchBuildVodVOWithoutQuality(vodInfoList, true);
    }

    private Map<Long, Long> getInteractiveTimes(String setKey, String uid, List<VodVO> vodVOList) {
        Map<Long, Long> timeMap = new HashMap<>();
        setKey = setKey.concat(uid);
        String finalSetKey = setKey;
        vodVOList.forEach(vod -> {
                    Double score = redisTemplate.opsForZSet().score(finalSetKey, vod.getCid().toString());
                    if (Objects.isNull(score)) {
                        return;
                    }
                    timeMap.put(vod.getCid(), score.longValue());
                }
        );
        return timeMap;
    }

    @Override
    public void appendCollectTime(String uid, List<VodVO> vodVOList) {
        Map<Long, Long> interactiveTimes = getInteractiveTimes(VodCacheKeyEnum.SetKey.COLLECT_SET, uid, vodVOList);
        vodVOList.forEach(vod -> vod.setCollectTime(interactiveTimes.getOrDefault(vod.getCid(), 0L)));
    }

    @Override
    public void appendCoinTime(String uid, List<VodVO> vodVOList) {
        Map<Long, Long> interactiveTimes = getInteractiveTimes(VodCacheKeyEnum.SetKey.COIN_SET, uid, vodVOList);
        vodVOList.forEach(vod -> vod.setCoinTime(interactiveTimes.getOrDefault(vod.getCid(), 0L)));
    }

    @Override
    public void appendLikeTime(String uid, List<VodVO> vodVOList) {
        Map<Long, Long> interactiveTimes = getInteractiveTimes(VodCacheKeyEnum.SetKey.LIKE_SET, uid, vodVOList);
        vodVOList.forEach(vod -> vod.setLikeTime(interactiveTimes.getOrDefault(vod.getCid(), 0L)));
    }

    @Override
    public String announcement(String announcement) {
        String uid = (String) StpUtil.getLoginId();
        User user = mongoTemplate.findById(uid, User.class);
        if (Objects.isNull(user)) {
            log.error("uid：{} 不存在", uid);
            throw BusinessException.businessError("用户不存在");
        }
        user.setAnnouncement(announcement);
        mongoTemplate.save(user);
        return announcement;
    }

    @Override
    public UserVO updateUserInfo(UserUpdateDTO userUpdateDTO) {
        String uid = (String) StpUtil.getLoginId();
        User user = mongoTemplate.findById(uid, User.class);
        if (Objects.isNull(user)) {
            log.error("用户不存在，uid: {}", uid);
            throw BusinessException.businessError("用户信息异常");
        }
        user.setNickname(userUpdateDTO.getNickName())
                .setIntro(userUpdateDTO.getIntro())
                .setAvatar(userUpdateDTO.getAvatar())
                .setAnnouncement(userUpdateDTO.getAnnouncement());
        mongoTemplate.save(user);

        return userVO(uid, true);
    }
}
