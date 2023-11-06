package com.jerry.pilipala.domain.vod.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.jerry.pilipala.application.dto.CommentDTO;
import com.jerry.pilipala.application.vo.CommentVO;
import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodStatics;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Comment;
import com.jerry.pilipala.domain.vod.repository.CommentRepository;
import com.jerry.pilipala.domain.vod.repository.VodInfoRepository;
import com.jerry.pilipala.domain.vod.service.CommentService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final MongoTemplate mongoTemplate;
    private final CommentRepository commentRepository;
    private final VodInfoRepository vodInfoRepository;
    private final UserEntityRepository userEntityRepository;

    public CommentServiceImpl(MongoTemplate mongoTemplate,
                              CommentRepository commentRepository,
                              VodInfoRepository vodInfoRepository,
                              UserEntityRepository userEntityRepository) {
        this.mongoTemplate = mongoTemplate;
        this.commentRepository = commentRepository;
        this.vodInfoRepository = vodInfoRepository;
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public CommentVO post(CommentDTO commentDTO) {
        String uid = (String) StpUtil.getLoginId();

        // 查询作者信息
        User author = mongoTemplate.findById(new ObjectId(uid), User.class);

        Comment parentComment;

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

        Comment comment = new Comment()
                .setUid(author.getUid().toString())
                .setContent(commentDTO.getContent())
                .setCid(commentDTO.getCid())
                .setParentId(commentDTO.getParentCommentId());

        comment = mongoTemplate.save(comment);

        // 更新评论数
        mongoTemplate.upsert(new Query(Criteria.where("_id").is(Long.parseLong(commentDTO.getCid()))
                        .and("date").is(DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd"))),
                new Update().inc("commentCount", 1), VodStatics.class);


        // 构建评论模型
        CommentVO commentVO = new CommentVO();

        PreviewUserVO authorPreviewVO = new PreviewUserVO().setUid(author.getUid().toString())
                .setAvatar(author.getAvatar())
                .setNickName(author.getNickname())
                .setIntro(author.getIntro());

        return commentVO.setId(comment.getId().toString())
                .setCid(comment.getCid())
                .setAuthor(authorPreviewVO)
                .setContent(comment.getContent())
                .setHasChild(false)
                .setDate(comment.getCtime());
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
                            .is(cid).and("parentId").is(""))
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
}
