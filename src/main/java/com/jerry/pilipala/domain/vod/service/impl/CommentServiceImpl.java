package com.jerry.pilipala.domain.vod.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.domain.vod.entity.neo4j.CommentEntity;
import com.jerry.pilipala.domain.vod.entity.neo4j.VodInfoEntity;
import com.jerry.pilipala.domain.vod.repository.CommentRepository;
import com.jerry.pilipala.domain.vod.repository.VodInfoRepository;
import com.jerry.pilipala.domain.vod.service.CommentService;
import com.jerry.pilipala.application.dto.CommentDTO;
import com.jerry.pilipala.domain.user.entity.mongo.User;

import com.jerry.pilipala.domain.vod.entity.mongo.vod.Comment;
import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.application.vo.CommentVO;
import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
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

        Comment comment = new Comment().setUid(uid)
                .setParentId(commentDTO.getParentCommentId())
                .setCid(commentDTO.getCid())
                .setContent(commentDTO.getContent())
                .setChildCount(0);
        comment = mongoTemplate.save(comment);

        if (StringUtils.isNotBlank(commentDTO.getParentCommentId())) {
            Update update = new Update();
            update.inc("childCount", 1);
            mongoTemplate.findAndModify(
                    new Query(Criteria.where("_id").is(comment.getParentId())),
                    update, Comment.class);
        }

        // 查询作者信息
        User author = mongoTemplate.findOne(new Query(
                        Criteria.where("_id").is(new ObjectId(uid))),
                User.class);
        if (Objects.isNull(author)) {
            author = User.UNKNOWN;
        }

        CommentEntity parentCommentEntity = null;

        if (StringUtils.isNotBlank(commentDTO.getParentCommentId())) {
            parentCommentEntity = commentRepository.findById(commentDTO.getParentCommentId())
                    .orElse(null);
        }

        VodInfoEntity vodInfoEntity = vodInfoRepository.findById(Long.parseLong(commentDTO.getCid()))
                .orElse(null);

        UserEntity userEntity = userEntityRepository.findById(comment.getUid()).orElse(null);

        // 保存 neo4j
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setId(comment.getId().toString())
                .setParentComment(parentCommentEntity)
                .setVod(vodInfoEntity)
                .setContent(comment.getContent())
                .setAuthor(userEntity)
                .setCtime(comment.getCtime());
        commentRepository.save(commentEntity);
        return buildCommentVO(comment, author);
    }

    @Override
    public Page<CommentVO> get(String cid,
                               String parentCommentId,
                               Integer pageNo,
                               Integer pageSize) {
        Criteria criteria = Criteria.where("cid").is(cid);

        if (StringUtils.isNotBlank(parentCommentId)) {
            criteria.and("parentId").is(parentCommentId);
        } else {
            criteria.and("parentId").is("");
        }
        // 查询分页数据
        Query query = new Query(criteria).skip((long) Math.max(0, pageNo - 1) * pageSize);
        List<Comment> commentList = mongoTemplate.find(query, Comment.class);

        Set<String> uidSet = commentList.stream().map(Comment::getUid).collect(Collectors.toSet());
        List<User> userList = mongoTemplate.find(new Query(Criteria.where("_id").in(uidSet)), User.class);
        Map<String, User> userMap = userList.stream().collect(Collectors.toMap(u -> u.getUid().toString(), u -> u));

        List<CommentVO> list = commentList.stream().map(comment -> {
            // 查询作者信息
            User author = userMap.getOrDefault(comment.getUid(), null);
            if (Objects.isNull(author)) {
                author = User.UNKNOWN;
            }
            return buildCommentVO(comment, author);
        }).toList();

        // 查询总数
        long total = mongoTemplate.count(new Query(criteria), Comment.class);
        return new Page<CommentVO>()
                .setPageNo(pageNo)
                .setPageSize(pageSize)
                .setTotal(total)
                .setPage(list);
    }

    private CommentVO buildCommentVO(Comment comment, User author) {
        CommentVO commentVO = new CommentVO();

        PreviewUserVO authorPreviewVO = new PreviewUserVO().setUid(author.getUid().toString())
                .setAvatar(author.getAvatar())
                .setNickName(author.getNickname())
                .setIntro(author.getIntro());

        return commentVO.setId(comment.getId().toString())
                .setCid(comment.getCid())
                .setAuthor(authorPreviewVO)
                .setContent(comment.getContent())
                .setChildCount(comment.getChildCount())
                .setDate(comment.getCtime());
    }
}
