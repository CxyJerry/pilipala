package com.jerry.pilipala.domain.vod.service.scheduler;

import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodStatistics;
import com.jerry.pilipala.domain.vod.entity.neo4j.VodInfoEntity;
import com.jerry.pilipala.domain.vod.repository.VodInfoRepository;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.utils.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InteractiveActionScheduler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final VodInfoRepository vodInfoRepository;
    private final JsonHelper jsonHelper;

    public InteractiveActionScheduler(RedisTemplate<String, Object> redisTemplate, MongoTemplate mongoTemplate, VodInfoRepository vodInfoRepository, JsonHelper jsonHelper) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
        this.vodInfoRepository = vodInfoRepository;
        this.jsonHelper = jsonHelper;
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void schedule() {
        try {
            log.info("schedule task start");
            Map<Object, Object> vodStatisticsMap = redisTemplate.opsForHash().entries(VodCacheKeyEnum.HashKey.VOD_INFO_CACHE_KEY);

            List<Long> cidList = vodStatisticsMap.keySet().stream().map(Object::toString).map(Long::parseLong).toList();
            Map<Long, VodInfoEntity> vodInfoEntityMap = vodInfoRepository.findAllById(cidList)
                    .stream()
                    .collect(Collectors.toMap(VodInfoEntity::getCid, v -> v));

            vodStatisticsMap.forEach((cid, statistics) -> {
                VodStatistics vodStatistics = jsonHelper.convert(statistics, VodStatistics.class);
                mongoTemplate.save(vodStatistics);
                VodInfoEntity vodInfoEntity = vodInfoEntityMap.get(Long.parseLong(cid.toString()));
                if (Objects.isNull(vodInfoEntity)) {
                    return;
                }
                vodInfoEntity.setViewCount(vodStatistics.getViewCount())
                        .setLikeCount(vodStatistics.getLikeCount())
                        .setCollectCount(vodStatistics.getCollectCount())
                        .setCommentCount(vodStatistics.getCommentCount())
                        .setCoinCount(vodStatistics.getCoinCount())
                        .setBarrageCount(vodStatistics.getBarrageCount())
                        .setShareCount(vodStatistics.getShareCount());
            });
            Collection<VodInfoEntity> values = vodInfoEntityMap.values();
            vodInfoRepository.saveAll(values);
            log.info("schedule task end");
        } catch (Exception e) {
            log.error("定时刷新互动数据失败，", e);
        }
    }
}
