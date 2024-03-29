package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.CommentInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.domain.vod.service.CommentService;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommentInteractiveActionHandler extends InteractiveActionHandler {
    private final CommentService commentService;

    public CommentInteractiveActionHandler(MongoTemplate mongoTemplate,
                                           RedisTemplate<String, Object> redisTemplate,
                                           CommentService commentService) {
        super(mongoTemplate, redisTemplate);
        this.commentService = commentService;
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        VodInteractiveAction interactiveAction = super.handle(interactiveParam);
        CommentInteractiveParam param = (CommentInteractiveParam) interactiveParam;
        String cid = param.getCid().toString();

        // 推送评论
        commentService.post(param.getSelfUid(), param.getComment());

        // 更新统计数据
        checkVodStatisticsExists(cid);
        incVodStatistics(cid, "commentCount", true);

        return interactiveAction;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.COMMENT;
    }
}
