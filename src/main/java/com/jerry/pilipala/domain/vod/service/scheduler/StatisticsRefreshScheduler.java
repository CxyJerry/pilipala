package com.jerry.pilipala.domain.vod.service.scheduler;

import cn.hutool.core.date.DateUtil;
import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodStatics;
import com.jerry.pilipala.domain.vod.entity.neo4j.VodInfoEntity;
import com.jerry.pilipala.domain.vod.repository.VodInfoRepository;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StatisticsRefreshScheduler {
    private final MongoTemplate mongoTemplate;
    private final VodInfoRepository vodInfoRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public StatisticsRefreshScheduler(MongoTemplate mongoTemplate,
                                      VodInfoRepository vodInfoRepository,
                                      RedisTemplate<String, Object> redisTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.vodInfoRepository = vodInfoRepository;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshStatistics() {
        List<VodStatics> vodStaticsList = mongoTemplate.find(new Query(Criteria.where("date")
                        .is(DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd"))),
                VodStatics.class);
        List<Long> cidList = vodStaticsList.stream().map(VodStatics::getCid).toList();
        Map<Long, VodStatics> vodStaticsMap = vodStaticsList.stream()
                .collect(Collectors.toMap(VodStatics::getCid, v -> v));

        List<VodInfoEntity> vodInfoEntities = vodInfoRepository.findAllById(cidList);
        vodInfoEntities.forEach(vodStaticsEntity -> {
            VodStatics vodStatics = vodStaticsMap.get(vodStaticsEntity.getCid());
            vodStaticsEntity.setViewCount(vodStaticsEntity.getViewCount() + vodStatics.getViewCount())
                    .setLikeCount(vodStaticsEntity.getLikeCount() + vodStatics.getLikeCount())
                    .setBarrageCount(vodStaticsEntity.getBarrageCount() + vodStatics.getBarrageCount())
                    .setCommentCount(vodStaticsEntity.getCommentCount() + vodStatics.getCommentCount())
                    .setCoinCount(vodStaticsEntity.getCoinCount() + vodStatics.getCoinCount())
                    .setCollectCount(vodStaticsEntity.getCollectCount() + vodStatics.getCollectCount())
                    .setShareCount(vodStaticsEntity.getShareCount() + vodStatics.getShareCount());
            vodInfoRepository.save(vodStaticsEntity);
        });
        redisTemplate.opsForHash().delete(VodCacheKeyEnum.HashKey.VOD_INFO_CACHE_KEY,
                cidList.stream().map(String::valueOf).toList());
    }
}
