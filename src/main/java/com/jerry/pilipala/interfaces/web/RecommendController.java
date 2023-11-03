package com.jerry.pilipala.interfaces.web;

import com.jerry.pilipala.infrastructure.annotations.IgnoreLog;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.PartitionEnum;
import com.jerry.pilipala.domain.vod.service.RecommendService;
import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.application.vo.RecommendVO;
import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("recommend")
public class RecommendController {
    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/get")
    public CommonResponse<?> recommend(@RequestParam("swiper_count")
                                       @Max(value = 10, message = "非法数量")
                                       @Min(value = 1, message = "非法数量") Integer swiperCount,

                                       @RequestParam("feed_count")
                                       @Max(value = 10, message = "非法数量")
                                       @Min(value = 1, message = "非法数量") Integer feedCount,

                                       @RequestParam("recommend_count_per_part")
                                       @Max(value = 10, message = "非法数量")
                                       @Min(value = 1, message = "非法数量") Integer recommendCountPerPart) {
        RecommendVO recommendVO = recommendService.recommend(swiperCount, feedCount, recommendCountPerPart);
        return CommonResponse.success(recommendVO);
    }

    @GetMapping("/partition")
    public CommonResponse<?> partition(@RequestParam("partition") String partition,
                                       @RequestParam("order_by") String orderBy,
                                       @RequestParam("page_no") Integer pageNo,
                                       @RequestParam("page_size") Integer pageSize) {
        Page<PreviewBVodVO> previewBVodVOS = recommendService.recommendPartition(
                partition,
                orderBy,
                pageNo,
                pageSize
        );
        return CommonResponse.success(previewBVodVOS);
    }

    @IgnoreLog
    @GetMapping("/partitions")
    public CommonResponse<?> partitions() {
        Map<String, List<PartitionEnum>> partitions = recommendService.partitions();
        return CommonResponse.success(partitions);
    }
}
