package com.jerry.pilipala.domain.vod.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerry.pilipala.domain.vod.service.RecommendService;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.BVod;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.infrastructure.enums.PartitionEnum;
import com.jerry.pilipala.infrastructure.enums.VodOrderByEnum;
import com.jerry.pilipala.infrastructure.enums.VodStatusEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.infrastructure.utils.Pair;
import com.jerry.pilipala.application.vo.RecommendVO;
import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import org.apache.logging.log4j.util.Strings;
import org.neo4j.driver.GraphDatabase;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper;

    private final VodService vodService;


    public RecommendServiceImpl(MongoTemplate mongoTemplate,
                                RedisTemplate<String, Object> redisTemplate,
                                ObjectMapper mapper,
                                VodService vodService) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
        this.vodService = vodService;
    }

    @Override
    public RecommendVO recommend(Integer swiperCount, Integer feedCount, Integer recommendCountPerPartition) {
        List<PartitionEnum> allPartitions = PartitionEnum.partitions();
        List<String> partitions = allPartitions
                .stream()
                .collect(Collectors.groupingBy(PartitionEnum::getPartition))
                .keySet()
                .stream()
                .toList();


        RecommendVO recommendVO = new RecommendVO();
        List<PreviewBVodVO> swiper = randomPreviewBVodList(swiperCount, null, null);
        List<String> swiperBvIds = swiper.stream().map(PreviewBVodVO::getBvId).toList();
        List<PreviewBVodVO> first = randomPreviewBVodList(feedCount, null, swiperBvIds);
        List<String> firstBvIds = new ArrayList<>(first.stream().map(PreviewBVodVO::getBvId).toList());
        firstBvIds.addAll(swiperBvIds);

        List<Pair<String, List<PreviewBVodVO>>> recommends = new ArrayList<>();
        partitions.forEach(p -> {
            List<PreviewBVodVO> previewBVodVOS = randomPreviewBVodList(recommendCountPerPartition, p, firstBvIds);
            Pair<String, List<PreviewBVodVO>> pair = new Pair<String, List<PreviewBVodVO>>().setKey(p).setValue(previewBVodVOS);
            if (!previewBVodVOS.isEmpty()) {
                recommends.add(pair);
            }
        });
        recommendVO.setSwiper(swiper).setFirst(first).setTypes(recommends);
        return recommendVO;
    }

    private List<PreviewBVodVO> randomPreviewBVodList(Integer count, String partition, List<String> exclude) {
        List<Object> bvIdList = new ArrayList<>();
        // 填写了分区
        List<Object> members;
        if (Strings.isNotBlank(partition)) {
            members = redisTemplate.opsForSet().randomMembers(partition, count);
        }
        // 未填写分区
        else {
            members = redisTemplate.opsForSet().randomMembers("all_bvod", count);
        }
        if (Objects.nonNull(members)) {
            bvIdList.addAll(members);
        }

        // 查询出对应的 bvod
        List<BVod> bvodList = mongoTemplate.find(new Query(Criteria.where("_id").in(bvIdList)), BVod.class);


        // 分组查询出每个 bvid 的首个可播放的 vod 信息
        Set<String> bvIdSet = bvodList.stream().map(BVod::getBvId).collect(Collectors.toSet());
        Criteria vodInfoCriteria = Criteria.where("bvId").in(bvIdSet).and("status").is(VodStatusEnum.PASSED);
        MatchOperation match = Aggregation.match(vodInfoCriteria);
        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "mtime");
        GroupOperation group = Aggregation.group("bvId").first("$$ROOT").as("firstDocument");
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                sort,
                group,
                Aggregation.replaceRoot().withValueOf("$firstDocument")
        );
        List<VodInfo> vodInfoList = mongoTemplate.aggregate(aggregation, "vod_info", VodInfo.class).getMappedResults();

        return vodService.buildPreviewBVodList(vodInfoList);
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
        Page<PreviewBVodVO> page = new Page<>();
        page.setPageNo(pageNo).setPageSize(pageSize);
        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, VodOrderByEnum.parse(orderBy));
        Criteria criteria = Criteria.where("partition").is(partition)
                .and("status").is(VodStatusEnum.PASSED);

        long count = mongoTemplate.count(new Query(criteria), VodInfo.class);
        page.setTotal(count);
        if (count == 0) {
            return page.setPage(new ArrayList<>());
        }

        MatchOperation match = Aggregation.match(criteria);
        SkipOperation skip = Aggregation.skip((long) Math.max(pageNo - 1, 0) * pageSize);
        LimitOperation limit = Aggregation.limit(pageSize);
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                sort,
                skip,
                limit
        );
        List<VodInfo> vodInfoList = mongoTemplate.aggregate(aggregation, "vod_info", VodInfo.class)
                .getMappedResults();

        List<PreviewBVodVO> previewBVodVOS = vodService.buildPreviewBVodList(vodInfoList);
        return page.setPage(previewBVodVOS);
    }
}
