package com.jerry.pilipala.domain.vod.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.jerry.pilipala.application.dto.DanmakuDTO;
import com.jerry.pilipala.application.vo.vod.DanmakuValueVO;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.user.repository.UserEntityRepository;
import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodStatics;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Danmaku;
import com.jerry.pilipala.domain.vod.service.DanmakuService;
import com.jerry.pilipala.domain.vod.service.DanmakuSseManager;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.infrastructure.utils.RequestUtil;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DanmakuServiceImpl implements DanmakuService {

    private final MongoTemplate mongoTemplate;
    private final HttpServletRequest request;
    private final UserEntityRepository userEntityRepository;

    private final DanmakuSseManager danmakuSseManager;

    public DanmakuServiceImpl(MongoTemplate mongoTemplate,
                              HttpServletRequest request,
                              UserEntityRepository userEntityRepository,
                              DanmakuSseManager danmakuSseManager) {
        this.mongoTemplate = mongoTemplate;
        this.request = request;
        this.userEntityRepository = userEntityRepository;
        this.danmakuSseManager = danmakuSseManager;
    }

    @Override
    public DanmakuValueVO send(DanmakuDTO danmakuDto) {
        String uid = (String) StpUtil.getLoginId();
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
                .setColor(danmakuDto.getColor())
                .setIp(RequestUtil.getIpAddress(request));
        danmaku = mongoTemplate.save(danmaku);

        // 更新弹幕数
        mongoTemplate.upsert(new Query(Criteria.where("_id").is(danmakuDto.getId())
                        .and("date").is(DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd"))),
                new Update().inc("barrageCount", 1), VodStatics.class);

        DanmakuValueVO danmakuValueVO = new DanmakuValueVO();
        danmakuValueVO.setUid(uid)
                .setVisible(danmaku.getVisible())
                .setColor(danmaku.getColor())
                .setTime(danmaku.getTime())
                .setText(danmaku.getText());

        danmakuSseManager.send(danmakuDto.getId(), danmakuValueVO);
        return danmakuValueVO;
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
