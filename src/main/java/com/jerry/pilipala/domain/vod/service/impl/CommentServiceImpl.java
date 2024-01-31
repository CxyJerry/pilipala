package com.jerry.pilipala.domain.vod.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.jerry.pilipala.application.dto.CommentDTO;
import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import com.jerry.pilipala.application.vo.vod.CommentVO;
import com.jerry.pilipala.domain.common.entity.Template;
import com.jerry.pilipala.domain.common.template.MessageTrigger;
import com.jerry.pilipala.domain.common.template.TemplateResolver;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Comment;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.domain.vod.service.CommentService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.enums.VodStatusEnum;
import com.jerry.pilipala.infrastructure.enums.message.MessageType;
import com.jerry.pilipala.infrastructure.enums.message.TemplateNameEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.infrastructure.utils.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    private final MongoTemplate mongoTemplate;
    private final MessageTrigger messageTrigger;
    private final TemplateResolver templateResolver;

    public CommentServiceImpl(MongoTemplate mongoTemplate,
                              MessageTrigger messageTrigger,
                              TemplateResolver templateResolver) {
        this.mongoTemplate = mongoTemplate;
        this.messageTrigger = messageTrigger;
        this.templateResolver = templateResolver;
    }

    @Override
    public void post(String uid, CommentDTO commentDTO) {
        // 查询作者信息
        User author = mongoTemplate.findById(new ObjectId(uid), User.class);

        Comment parentComment = null;

        if (StringUtils.isNotBlank(commentDTO.getParentCommentId())) {
            parentComment = mongoTemplate
                    .findById(new ObjectId(commentDTO.getParentCommentId()), Comment.class);
            if (Objects.isNull(parentComment)) {
                throw BusinessException.businessError("回复异常，该评论不存在");
            }
            parentComment.setHasChild(true);
            mongoTemplate.save(parentComment);
        }

        if (Objects.isNull(author)) {
            throw BusinessException.businessError("用户不存在");
        }

        // 检查下评论是否包含被 @ 的用户，如果有，需要做 html 的替换
        String content = commentDTO.getContent();
        Map<ObjectId, Pair<Integer, Integer>> userIdPairMap = extractUserIdPairMap(content);

        // 批量查一下数据库，如果用户ID存在，则替换成 html
        Collection<ObjectId> userIdCollection = new HashSet<>(userIdPairMap.keySet());
        Map<ObjectId, User> userMap = mongoTemplate.find(
                        new Query(Criteria.where("_id").in(userIdCollection)),
                        User.class)
                .stream()
                .collect(Collectors.toMap(User::getUid, Function.identity()));

        // 重新组装评论内容
        content = rebuildCommentContent(content, userIdCollection, userMap, userIdPairMap);

        Comment comment = new Comment()
                .setUid(author.getUid().toString())
                .setContent(content)
                .setCid(commentDTO.getCid())
                .setParentId(commentDTO.getParentCommentId());

        comment = mongoTemplate.save(comment);

        String cid = commentDTO.getCid();
        VodInfo vodInfo = mongoTemplate.findById(Long.parseLong(cid), VodInfo.class);
        if (Objects.isNull(vodInfo)) {
            throw BusinessException.businessError("稿件不存在");
        }

        // 推送被 @ 的消息
        triggerAtMessage(userMap.values(), author, comment, vodInfo);

        // 推送评论消息给视频作者
        triggerCommentMessage(author, comment, vodInfo);

        // 推送回复消息给评论作者
        triggerReplyMessage(comment, author, vodInfo, parentComment);
    }

    private String rebuildCommentContent(String content, Collection<ObjectId> userIdCollection, Map<ObjectId, User> userMap, Map<ObjectId, Pair<Integer, Integer>> userIdPairMap) {
        // content 防止注入
        content = content.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");

        // 重新组装评论内容
        StringBuilder builder = new StringBuilder(content);

        userIdCollection.forEach(userId -> {
            User user = userMap.get(userId);
            if (Objects.isNull(user)) {
                return;
            }

            // 将 @userId 替换成 <a href="/#/sphere/{userId}">@{nickname}</a>
            Pair<Integer, Integer> pair = userIdPairMap.get(userId);
            // 生成替换的 HTML
            Template template = mongoTemplate.findOne(
                    new Query(Criteria.where("_id").is(TemplateNameEnum.AT_TEMPLATE)),
                    Template.class);
            if (Objects.isNull(template)) {
                return;
            }
            HashMap<String, String> params = new HashMap<>();
            params.put("user_id", user.getUid().toString());
            params.put("nickname", user.getNickname());
            String replaceHtml = templateResolver.fillVariable(template.getContent(), params);

            // 将原始评论内容中的 @userId 替换为对应的 HTML
            builder.replace(pair.getKey(), pair.getValue(), replaceHtml);
        });
        content = builder.toString();
        return content;
    }

    private static Map<ObjectId, Pair<Integer, Integer>> extractUserIdPairMap(String content) {
        Pattern pattern = Pattern.compile("@\\S+\\s");
        Matcher matcher = pattern.matcher(content);
        // 匹配到的可能有多个值，遍历一遍，取出所有的用户ID，记录一下该匹配位置的起始&结束位置，
        Map<ObjectId, Pair<Integer, Integer>> userIdPairMap = new HashMap<>();
        while (matcher.find()) {
            String userId = matcher.group();
            userId = userId.substring(1, userId.length() - 1);
            int start = matcher.start();
            int end = matcher.end();
            try {
                ObjectId key = new ObjectId(userId);
                userIdPairMap.put(key, new Pair<>(start, end));
            } catch (Exception e) {
                log.warn("解析被 @ 的用户ID失败，", e);
            }
        }
        return userIdPairMap;
    }

    private void triggerAtMessage(Collection<User> userCollection, User author, Comment comment, VodInfo vodInfo) {
        // 遍历下 userIdList，然后从 usermap 尝试获取用户信息，如果可以获取到，则推送消息
        userCollection.forEach(user -> {
            if (Objects.nonNull(user)) {
                Map<String, String> variables = new HashMap<>();
                variables.put("user_id", author.getUid().toString());
                variables.put("avatar", author.getAvatar());
                variables.put("user_name", author.getNickname());
                variables.put("content", comment.getContent());
                variables.put("cover_url", vodInfo.getCoverUrl());
                variables.put("bvid", vodInfo.getBvId());
                variables.put("cid", vodInfo.getCid().toString());
                variables.put("time",
                        DateUtil.format(new Date(comment.getCtime()), "yyyy-MM-dd HH:mm:ss")
                );
                messageTrigger.trigger(
                        TemplateNameEnum.AT_NOTIFY,
                        MessageType.AT,
                        author.getUid().toString(),
                        user.getUid().toString(),
                        variables
                );
            }
        });
    }

    private void triggerCommentMessage(User author, Comment comment, VodInfo vodInfo) {
        String userId = StpUtil.getLoginId("");

        CompletableFuture.runAsync(() -> {
            try {
                // 视频作者本人发送的评论，不用推送消息给视频作者
                if (Objects.equals(userId, vodInfo.getUid())) {
                    return;
                }
                Map<String, String> variables = new HashMap<>();
                variables.put("user_id", author.getUid().toString());
                variables.put("avatar", author.getAvatar());
                variables.put("user_name", author.getNickname());
                variables.put("content", comment.getContent());
                variables.put("cover_url", vodInfo.getCoverUrl());
                variables.put("bvid", vodInfo.getBvId());
                variables.put("cid", vodInfo.getCid().toString());
                variables.put("time",
                        DateUtil.format(new Date(comment.getCtime()), "yyyy-MM-dd HH:mm:ss")
                );

                // 推送视频作者
                messageTrigger.trigger(
                        TemplateNameEnum.COMMENT_NOTIFY,
                        MessageType.REPLY,
                        comment.getUid(),
                        vodInfo.getUid(),
                        variables
                );

            } catch (Exception e) {
                log.error("推送消息失败,", e);
            }

        });

    }

    private void triggerReplyMessage(Comment comment, User author, VodInfo vodInfo, Comment originComment) {
        String userId = (String) StpUtil.getLoginId();
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> variables;
                // 推送父评论作者
                if (Objects.nonNull(comment.getParentId())) {
                    if (Objects.isNull(originComment)) {
                        throw BusinessException.businessError("评论不存在");
                    }
                    // 如果是回复自己的评论，不需要推送消息
                    if (Objects.equals(userId, originComment.getUid())) {
                        return;
                    }
                    variables = new HashMap<>();
                    variables.put("user_id", author.getUid().toString());
                    variables.put("avatar", author.getAvatar());
                    variables.put("user_name", author.getNickname());
                    variables.put("content", comment.getContent());
                    variables.put("cover_url", vodInfo.getCoverUrl());
                    variables.put("bvid", vodInfo.getBvId());
                    variables.put("cid", vodInfo.getCid().toString());
                    variables.put("origin_comment", originComment.getContent());
                    variables.put("time",
                            DateUtil.format(new Date(comment.getCtime()), "yyyy-MM-dd HH:mm:ss")
                    );

                    messageTrigger.trigger(
                            TemplateNameEnum.REPLY_NOTIFY,
                            MessageType.REPLY,
                            userId,
                            originComment.getUid(),
                            variables
                    );
                }
            } catch (Exception e) {
                log.error("消息触发失败，", e);
            }

        });
    }

    @Override
    public Page<CommentVO> get(String cid,
                               String parentCommentId,
                               Integer pageNo,
                               Integer pageSize) {
        List<Comment> comments;
        long total;
        if (StringUtils.isNotBlank(parentCommentId)) {
            comments = mongoTemplate.find(
                    new Query(Criteria.where("parentId")
                            .is(parentCommentId))
                            .skip((long) Math.max(0, pageNo - 1) * pageSize)
                            .limit(pageSize),
                    Comment.class);
            total = mongoTemplate.count(
                    new Query(Criteria.where("parentId")
                            .is(parentCommentId)),
                    Comment.class);
        } else {
            comments = mongoTemplate.find(
                    new Query(Criteria.where("cid")
                            .is(cid).and("parentId").in("", null))
                            .skip((long) Math.max(0, pageNo - 1) * pageSize)
                            .limit(pageSize),
                    Comment.class);
            total = mongoTemplate.count(
                    new Query(Criteria.where("cid")
                            .is(cid).and("parentId").is("")),
                    Comment.class);
        }
        // 获取全部评论作者信息
        List<String> uidList = comments.stream().map(Comment::getUid).toList();
        List<User> userList = mongoTemplate.find(new Query(Criteria.where("_id").in(uidList)), User.class);
        Map<String, User> userMap = userList.stream()
                .collect(Collectors.toMap(u -> u.getUid().toString(), u -> u));

        List<CommentVO> page = comments.stream().map(comment -> {
            User user = userMap.get(comment.getUid());
            PreviewUserVO author = new PreviewUserVO();
            author.setUid(user.getUid().toString())
                    .setAvatar(user.getAvatar())
                    .setIntro(user.getIntro())
                    .setNickName(user.getNickname());
            return new CommentVO().setId(comment.getId().toString())
                    .setContent(comment.getContent())
                    .setDate(comment.getCtime())
                    .setCid(comment.getCid())
                    .setAuthor(author)
                    .setHasChild(comment.getHasChild());
        }).toList();

        return new Page<CommentVO>()
                .setPage(page)
                .setTotal(total)
                .setPageNo(pageNo)
                .setPageSize(pageSize);
    }

    @Override
    public void delete(String uid, Long cid, String commentId) {
        VodInfo vodInfo = mongoTemplate.findById(cid, VodInfo.class);
        if (Objects.isNull(vodInfo) || !vodInfo.getStatus().equals(VodStatusEnum.PASSED)) {
            throw BusinessException.businessError("稿件不存在/未开放");
        }

        Comment comment = mongoTemplate.findById(new ObjectId(commentId), Comment.class);
        if (Objects.isNull(comment) || !comment.getDeleted()) {
            throw BusinessException.businessError("评论不存在/已删除");
        }
        String vodAuthorId = vodInfo.getUid();
        String commentAuthorId = comment.getUid();

        // 如果不是稿件作者 / 评论作者，无权删除评论
        if (!Objects.equals(vodAuthorId, uid) ||
                !Objects.equals(commentAuthorId, uid)) {
            throw BusinessException.businessError("您无权删除该评论");
        }

        Update update = new Update();
        update.set("deleted", true);
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(commentId)),
                update,
                Comment.class
        );
    }
}
