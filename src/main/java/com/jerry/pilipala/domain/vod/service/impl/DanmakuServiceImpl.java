package com.jerry.pilipala.domain.vod.service.impl;

import com.jerry.pilipala.application.dto.DanmakuDTO;
import com.jerry.pilipala.application.vo.vod.DanmakuValueVO;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Danmaku;
import com.jerry.pilipala.domain.vod.service.DanmakuService;
import com.jerry.pilipala.domain.vod.service.DanmakuSseManager;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DanmakuServiceImpl implements DanmakuService {
    private final MongoTemplate mongoTemplate;
    private final DanmakuSseManager danmakuSseManager;

    public DanmakuServiceImpl(MongoTemplate mongoTemplate,
                              DanmakuSseManager danmakuSseManager) {
        this.mongoTemplate = mongoTemplate;
        this.danmakuSseManager = danmakuSseManager;
    }

    @Override
    public void send(String uid, DanmakuDTO danmakuDto) {
        User sender = mongoTemplate.findById(new ObjectId(uid), User.class);

        if (Objects.isNull(sender)) {
            throw new BusinessException("用户不存在", StandardResponse.ERROR);
        }
        Danmaku danmaku = new Danmaku()
                .setCid(danmakuDto.getId())
                .setUid(uid)
                .setSender(sender.getNickname())
                .setTime(danmakuDto.getTime())
                .setText(danmakuDto.getText())
                .setColor(danmakuDto.getColor());
        danmaku = mongoTemplate.save(danmaku);

        DanmakuValueVO danmakuValueVO = new DanmakuValueVO();
        danmakuValueVO.setUid(uid)
                .setVisible(danmaku.getVisible())
                .setColor(danmaku.getColor())
                .setTime(danmaku.getTime())
                .setText(danmaku.getText());

        danmakuSseManager.send(danmakuDto.getId(), danmakuValueVO);
    }

    @Override
    public List<DanmakuValueVO> danmakus(Long cid, Integer max) {
        Query query = new Query(Criteria.where("cid").is(cid));
        if (max != 0) {
            query.limit(max);
        }
        List<Danmaku> danmakuList = mongoTemplate.find(
                query,
                Danmaku.class);
        return danmakuList.stream().map(danmaku ->
                new DanmakuValueVO().setTime(danmaku.getTime())
                        .setVisible(danmaku.getVisible())
                        .setColor(danmaku.getColor())
                        .setUid(danmaku.getUid())
                        .setText(danmaku.getText())
        ).toList();
    }
}
