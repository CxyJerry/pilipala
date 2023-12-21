package com.jerry.pilipala.domain.vod.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import com.jerry.pilipala.application.vo.vod.PreviewVodVO;
import com.jerry.pilipala.application.vo.vod.RecommendVO;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodStatistics;
import com.jerry.pilipala.domain.vod.entity.neo4j.VodInfoEntity;
import com.jerry.pilipala.domain.vod.repository.VodInfoRepository;
import com.jerry.pilipala.domain.vod.service.RecommendService;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.infrastructure.enums.PartitionEnum;
import com.jerry.pilipala.infrastructure.enums.Qn;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {
    private final MongoTemplate mongoTemplate;
    private final VodService vodService;
    private final VodInfoRepository vodInfoRepository;

    public RecommendServiceImpl(MongoTemplate mongoTemplate,
                                RedisTemplate<String, Object> redisTemplate,
                                VodService vodService,
                                VodInfoRepository vodInfoRepository) {
        this.mongoTemplate = mongoTemplate;
        this.vodService = vodService;
        this.vodInfoRepository = vodInfoRepository;
    }

    @Override
    public RecommendVO recommend(Integer swiperCount, Integer feedCount) {
        String uid = StpUtil.getLoginId("");
        List<VodInfoEntity> swiperList = vodInfoRepository
                .recommendVideosByContentBasedFiltering(swiperCount);

        List<VodInfoEntity> firstList =
                vodInfoRepository.recommendVideosByUserId(uid, feedCount);
        if (firstList.isEmpty()) {
            firstList = vodInfoRepository
                    .recommendVideosByContentBasedFiltering(swiperCount);
        }

        RecommendVO recommendVO = new RecommendVO();
        recommendVO.setSwiper(buildPreviewBVodList(swiperList))
                .setFirst(buildPreviewBVodList(firstList));

        return recommendVO;
    }


    @Override
    public Map<String, List<PartitionEnum>> partitions() {
        return PartitionEnum.partitions()
                .stream()
                .collect(Collectors.groupingBy(PartitionEnum::getPartition));
    }

    @Override
    public Page<PreviewBVodVO> recommendPartition(String partition,
                                                  String orderBy,
                                                  Integer pageNo,
                                                  Integer pageSize) {
        List<VodInfoEntity> vodInfoEntities = vodInfoRepository
                .recommendVideosByContentBasedFiltering(partition, Math.max(pageNo - 1, 0) * pageSize, pageSize);

        Long total = vodInfoRepository.countVideosByPartition(partition);
        Page<PreviewBVodVO> page = new Page<>();
        List<PreviewBVodVO> data = this.buildPreviewBVodList(vodInfoEntities);

        return page.setPageNo(pageNo).setPageSize(pageSize).setPage(data).setTotal(total);
    }

    public List<PreviewBVodVO> buildPreviewBVodList(List<VodInfoEntity> vodInfoEntities) {
        List<String> uidList = vodInfoEntities.stream().map(VodInfoEntity::getAuthorId).toList();
        List<User> userList = mongoTemplate.find(new Query(Criteria.where("_id").in(uidList)), User.class);
        Map<String, User> userEntityMap = userList.stream()
                .collect(Collectors.toMap(u -> u.getUid().toString(), u -> u));

        Set<Object> cidSet = vodInfoEntities.stream()
                .map(VodInfoEntity::getCid)
                .map(String::valueOf)
                .collect(Collectors.toSet());

        Map<Long, VodStatistics> vodStatisticsMap = vodService.batchQueryVodStatistics(cidSet);

        return vodInfoEntities.stream().map(vodInfoEntity -> {
                    // 创建作者预览模型
                    User user = userEntityMap.get(vodInfoEntity.getAuthorId());
                    PreviewUserVO author = new PreviewUserVO().setUid(user.getUid().toString())
                            .setNickName(user.getNickname())
                            .setAvatar(user.getAvatar())
                            .setIntro(user.getIntro());
                    // 创建视频预览模型
                    // 设置预览视频
                    String url = "/file/video/%s/%s/1".formatted(vodInfoEntity.getCid(), Qn._PREVIEW.getDescription());
                    PreviewVodVO preview = new PreviewVodVO().setCid(vodInfoEntity.getCid())
                            .setUrl(url)
                            .setName(vodInfoEntity.getTitle());
                    VodStatistics statics = vodStatisticsMap.getOrDefault(vodInfoEntity.getCid(), new VodStatistics());
                    // 组装
                    return new PreviewBVodVO()
                            .setBvId(vodInfoEntity.getBvId())
                            .setCoverUrl(vodInfoEntity.getCoverUrl())
                            .setTitle(vodInfoEntity.getTitle())
                            .setDesc(vodInfoEntity.getDesc())
                            .setPartition(vodInfoEntity.getPartition())
                            .setViewCount(statics.getViewCount())
                            .setLikeCount(statics.getLikeCount())
                            .setBarrageCount(statics.getBarrageCount())
                            .setCommentCount(statics.getCommentCount())
                            .setCoinCount(statics.getCoinCount())
                            .setCollectCount(statics.getCollectCount())
                            .setShareCount(statics.getShareCount())
                            .setAuthor(author)
                            .setPreview(preview)
                            .setDuration(vodInfoEntity.getDuration());

                }
        ).toList();

    }
}
