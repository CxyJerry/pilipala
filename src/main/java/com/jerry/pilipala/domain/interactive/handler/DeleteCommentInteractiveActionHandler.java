package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.DeleteCommentInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.domain.vod.service.CommentService;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeleteCommentInteractiveActionHandler extends InteractiveActionHandler {
    private final CommentService commentService;

    public DeleteCommentInteractiveActionHandler(MongoTemplate mongoTemplate,
                                                 RedisTemplate<String, Object> redisTemplate,
                                                 CommentService commentService) {
        super(mongoTemplate, redisTemplate);
        this.commentService = commentService;
    }

    @Override
    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        VodInteractiveAction interactiveAction = super.handle(interactiveParam);

        DeleteCommentInteractiveParam param = (DeleteCommentInteractiveParam) interactiveParam;
        String cid = param.getCid().toString();
        String commentId = param.getCommentId();

        // 逻辑删除评论
        commentService.delete(interactiveParam.getSelfUid(), param.getCid(), commentId);

        // 更新统计数据
        checkVodStatisticsExists(cid);
        incVodStatistics(cid, "commentCount", false);

        return interactiveAction;
    }

    @Override
    public VodInteractiveActionEnum action() {
        return VodInteractiveActionEnum.DELETE_COMMENT;
    }
}
