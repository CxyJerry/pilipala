package com.jerry.pilipala.domain.message.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.vo.message.MessageVO;
import com.jerry.pilipala.application.vo.message.TemplateVO;
import com.jerry.pilipala.application.vo.message.UnreadMessageCountVO;
import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import com.jerry.pilipala.domain.common.entity.Template;
import com.jerry.pilipala.domain.message.entity.Message;
import com.jerry.pilipala.domain.message.service.MessageService;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.infrastructure.enums.MessageStatusEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MessageServiceImpl implements MessageService {
    private final MongoTemplate mongoTemplate;

    public MessageServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void saveMessageTemplate(String templateName, String content) {
        String authorId = StpUtil.getLoginId("");
        Template template = new Template();
        template.setName(templateName)
                .setContent(content)
                .setAuthorId(authorId);
        mongoTemplate.save(template);
    }

    @Override
    public List<TemplateVO> messageTemplates() {
        return mongoTemplate.findAll(Template.class)
                .stream()
                .map(template -> new TemplateVO()
                        .setName(template.getName())
                        .setContent(template.getContent())
                        .setAuthorId(template.getAuthorId())
                        .setCtime(template.getCtime()))
                .toList();
    }

    @Override
    public List<UnreadMessageCountVO> unreadCount(String uid) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("status").is(MessageStatusEnum.UNREAD.getStatus())
                                .and("receiverId").is(uid)
                ),
                Aggregation.group("type")
                        .first("type").as("type")
                        .count().as("count")
        );

        return mongoTemplate
                .aggregate(aggregation,
                        "message",
                        UnreadMessageCountVO.class)
                .getMappedResults();
    }

    @Override
    public Page<MessageVO> page(String type, int pageNo, int pageSize) {
        String uid = StpUtil.getLoginId("");
        Page<MessageVO> page = new Page<>();
        page.setPageNo(pageNo).setPageSize(pageSize);
        if (StringUtils.isBlank(uid)) {
            return page;
        }
        // 分页查询消息
        Criteria criteria = Criteria.where("receiverId").is(uid)
                .and("type").is(type);
        Query query = new Query(criteria)
                .with(Sort.by(Sort.Order.desc("ctime")))
                .skip((long) Math.max(0, pageNo - 1) * pageSize)
                .limit(pageSize);
        List<Message> messages = mongoTemplate.find(
                query,
                Message.class);

        // 查询过的消息可直接认为已读
        List<ObjectId> midList = messages.stream().map(Message::getId).toList();
        mongoTemplate.updateMulti(
                new Query(Criteria.where("_id").in(midList)),
                new Update().set("status", MessageStatusEnum.READ.getStatus()),
                Message.class);

        // 查询消息总数
        long total = mongoTemplate.count(new Query(criteria), Message.class);

        // 查询发送者信息
        List<ObjectId> uidList = messages.stream()
                .map(Message::getSenderId)
                .filter(StringUtils::isNotBlank)
                .map(ObjectId::new)
                .toList();
        List<User> senderList = mongoTemplate.find(
                new Query(Criteria.where("_id").in(uidList)),
                User.class);

        Map<String, User> senderMap = senderList.stream()
                .collect(Collectors.toMap(u -> u.getUid().toString(), Function.identity()));

        // 构建消息实体
        List<MessageVO> messageVOList = messages.stream().map(msg -> {
            User sender = senderMap.getOrDefault(msg.getSenderId(), null);
            PreviewUserVO senderPreviewVO = new PreviewUserVO();
            if (Objects.nonNull(sender)) {
                senderPreviewVO.setUid(msg.getSenderId())
                        .setAvatar(sender.getAvatar())
                        .setIntro(sender.getIntro())
                        .setNickName(sender.getNickname());
            } else {
                senderPreviewVO.setNickName("系统");
            }
            return new MessageVO()
                    .setId(msg.getId().toString())
                    .setSender(senderPreviewVO)
                    .setContent(msg.getContent())
                    .setCtime(msg.getCtime());
        }).toList();

        page.setPage(messageVOList).setTotal(total);
        return page;
    }

    @Override
    public void deleteMessageTemplate(String name) {
        mongoTemplate.remove(new Query(Criteria.where("_id").is(name)), Template.class);
    }
}
