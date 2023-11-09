package com.jerry.pilipala.domain.vod.service;

import com.jerry.pilipala.application.vo.vod.RecommendVO;
import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import com.jerry.pilipala.infrastructure.enums.PartitionEnum;
import com.jerry.pilipala.infrastructure.utils.Page;

import java.util.List;
import java.util.Map;

public interface RecommendService {

    RecommendVO recommend(Integer swiperCount, Integer feedCount);

    Map<String, List<PartitionEnum>> partitions();

    Page<PreviewBVodVO> recommendPartition(String partition, String orderBy, Integer pageNo, Integer pageSize);
}
