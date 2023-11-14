package com.jerry.pilipala.interfaces.web;

import com.jerry.pilipala.application.vo.vod.RecommendVO;
import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import com.jerry.pilipala.domain.vod.service.RecommendService;
import com.jerry.pilipala.infrastructure.annotations.IgnoreLog;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.PartitionEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
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

    /**
     * 获取推荐列表
     *
     * @param swiperCount 滚动热荐数量
     * @param feedCount   投喂数量
     * @return recommend
     */
    @ApiOperation("获取推荐列表")
    @GetMapping("/get")
    public CommonResponse<?> recommend(@RequestParam("swiper_count")
                                       @Max(value = 10, message = "非法数量")
                                       @Min(value = 1, message = "非法数量") Integer swiperCount,

                                       @RequestParam("feed_count")
                                       @Max(value = 10, message = "非法数量")
                                       @Min(value = 1, message = "非法数量") Integer feedCount) {
        RecommendVO recommendVO = recommendService.recommend(swiperCount, feedCount);
        return CommonResponse.success(recommendVO);
    }

    /**
     * 分区推荐
     *
     * @param partition 分区
     * @param orderBy   排序
     * @param pageNo    页码
     * @param pageSize  数量
     * @return page
     */
    @ApiOperation("分区推荐")
    @GetMapping("/partition")
    public CommonResponse<?> partition(@RequestParam("partition")
                                       @NotBlank(message = "分区不得为空") String partition,
                                       @RequestParam("order_by") String orderBy,
                                       @RequestParam("page_no")
                                       @Min(value = 1, message = "最小1")
                                       @Max(value = 1000, message = "最大1000") Integer pageNo,
                                       @RequestParam("page_size")
                                       @Min(value = 1, message = "最小1")
                                       @Max(value = 1000, message = "最大1000") Integer pageSize) {
        Page<PreviewBVodVO> previewBVodVOS = recommendService.recommendPartition(
                partition,
                orderBy,
                pageNo,
                pageSize
        );
        return CommonResponse.success(previewBVodVOS);
    }

    /**
     * 获取全部分区
     *
     * @return map
     */
    @ApiOperation("获取全部分区")
    @IgnoreLog
    @GetMapping("/partitions")
    public CommonResponse<?> partitions() {
        Map<String, List<PartitionEnum>> partitions = recommendService.partitions();
        return CommonResponse.success(partitions);
    }
}
