package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BarrageInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.domain.vod.service.DanmakuService;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DanmakuInteractiveActionHandler extends InteractiveActionHandler {
    private final DanmakuService danmakuService;

    public DanmakuInteractiveActionHandler(MongoTemplate mongoTemplate,
                                           RedisTemplate<String, Object> redisTemplate,
                                           DanmakuService danmakuService) {
        super(mongoTemplate, redisTemplate);
        this.danmakuService = danmakuService;
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        VodInteractiveAction interactiveAction = super.handle(interactiveParam);
        BarrageInteractiveParam param = (BarrageInteractiveParam) interactiveParam;

        // 推送弹幕数据
        danmakuService.send(param.getSelfUid(), param.getDanmaku());

        // 更新统计数据
        String cid = param.getCid().toString();
        checkVodStatisticsExists(cid);
        incVodStatistics(cid, "barrageCount", true);
        return interactiveAction;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.BARRAGE;
    }
}
