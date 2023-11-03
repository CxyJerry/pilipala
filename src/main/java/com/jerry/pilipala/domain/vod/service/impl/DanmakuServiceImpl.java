package com.jerry.pilipala.domain.vod.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.Danmaku;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import com.jerry.pilipala.application.dto.DanmakuDTO;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.vod.service.DanmakuService;
import com.jerry.pilipala.infrastructure.utils.RequestUtil;
import com.jerry.pilipala.application.vo.DanmakuVO;
import com.jerry.pilipala.application.vo.DanmakuValueVO;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@Service
public class DanmakuServiceImpl implements DanmakuService {

    private final MongoTemplate mongoTemplate;
    private final HttpServletRequest request;

    public DanmakuServiceImpl(MongoTemplate mongoTemplate,
                              HttpServletRequest request) {
        this.mongoTemplate = mongoTemplate;
        this.request = request;
    }

    @Override
    public DanmakuVO send(DanmakuDTO danmakuDto) {
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

        return new DanmakuVO().set_id(danmaku.getId().toString())
                .setPlayer(danmaku.getCid().toString())
                .setTime(danmaku.getTime())
                .setText(danmaku.getText())
                .setColor(danmaku.getColor())
                .setIp(danmaku.getIp())
                .setDate(danmaku.getCtime())
                .set__v(0);
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
