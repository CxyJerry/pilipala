package com.jerry.pilipala.domain.interactive.handler;

import cn.hutool.core.date.DateUtil;
import com.jerry.pilipala.domain.common.template.MessageTrigger;
import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.VodInteractiveParam;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.enums.message.MessageType;
import com.jerry.pilipala.infrastructure.enums.message.TemplateNameEnum;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
public class LikeInteractiveActionHandler extends InteractiveActionHandler {
    private final MessageTrigger messageTrigger;

    public LikeInteractiveActionHandler(MongoTemplate mongoTemplate,
                                        RedisTemplate<String, Object> redisTemplate,
                                        MessageTrigger messageTrigger) {
        super(mongoTemplate, redisTemplate);
        this.messageTrigger = messageTrigger;
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        // 事件记录
        VodInteractiveAction interactiveAction = super.handle(interactiveParam);
        VodInteractiveParam param = (VodInteractiveParam) interactiveParam;

        String cid = param.getCid().toString();

        // 是取消点赞事件，点赞数 - 1，set 移除 uid
        String uid = interactiveAction.getUid();
        String likeSetKey = VodCacheKeyEnum.SetKey.LIKE_SET.concat(String.valueOf(uid));
        if (Objects.nonNull(redisTemplate.opsForZSet().score(likeSetKey, cid))) {
            redisTemplate.opsForSet().remove(
                    likeSetKey,
                    cid
            );
            checkVodStatisticsExists(cid);
            incVodStatistics(cid, "likeCount", false);
        }
        // 如果是点赞事件，点赞数 +1，uid 放入 set
        else {
            redisTemplate.opsForZSet().add(
                    likeSetKey,
                    cid,
                    System.currentTimeMillis()
            );
            checkVodStatisticsExists(cid);
            incVodStatistics(cid, "likeCount", true);

            CompletableFuture.runAsync(() -> {
                VodInfo vodInfo = mongoTemplate.findById(Long.parseLong(cid), VodInfo.class);
                if (Objects.isNull(vodInfo)) {
                    throw BusinessException.businessError("稿件不存在");
                }
                User user = mongoTemplate.findById(new ObjectId(vodInfo.getUid()), User.class);
                if (Objects.isNull(user)) {
                    throw BusinessException.businessError("用户不存在");
                }
                Map<String, String> variables = new HashMap<>();
                variables.put("avatar", user.getAvatar());
                variables.put("user_name", user.getNickname());
                variables.put("user_id", user.getUid().toString());
                variables.put("time",
                        DateUtil.format(
                                new Date(interactiveAction.getCtime()),
                                "yyyy年MM月dd日 HH:mm:ss")
                );
                variables.put("bvid", vodInfo.getBvId());
                variables.put("cid", cid);
                variables.put("cover_url", vodInfo.getCoverUrl());
                messageTrigger.trigger(
                        TemplateNameEnum.LIKE_NOTIFY,
                        MessageType.LIKE,
                        uid,
                        vodInfo.getUid(),
                        variables
                );
            });

        }

        return interactiveAction;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.LIKE;
    }
}
