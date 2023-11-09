package com.jerry.pilipala.domain.message.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.vo.message.MessageVO;
import com.jerry.pilipala.application.vo.user.PreviewUserVO;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MessageServiceImpl implements MessageService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    public MessageServiceImpl(RedisTemplate<String, Object> redisTemplate,
                              MongoTemplate mongoTemplate) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void send(String senderId, String receiverId, String message) {
        Message msg = new Message();
        msg.setSenderId(senderId)
                .setReceiverId(receiverId)
                .setContent(message);
        mongoTemplate.save(msg);
    }

    @Override
    public long unreadCount(String uid) {
        return mongoTemplate.count(new Query(
                        Criteria.where("status").is(MessageStatusEnum.UNREAD.getStatus())
                                .and("receiverId").is(uid)),
                Message.class);
    }

    @Override
    public Page<MessageVO> page(int pageNo, int pageSize) {
        String uid = StpUtil.getLoginId("");
        Page<MessageVO> page = new Page<>();
        page.setPageNo(pageNo).setPageSize(pageSize);
        if (StringUtils.isBlank(uid)) {
            return page;
        }
        // 分页查询消息
        Criteria criteria = Criteria.where("receiverId").is(uid);
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
                .collect(Collectors.toMap(u -> u.getUid().toString(), u -> u));


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

}
