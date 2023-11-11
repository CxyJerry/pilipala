package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.dto.DanmakuDTO;
import com.jerry.pilipala.application.vo.vod.DanmakuResponse;
import com.jerry.pilipala.application.vo.vod.DanmakuValueVO;
import com.jerry.pilipala.domain.vod.service.DanmakuService;
import com.jerry.pilipala.domain.vod.service.DanmakuSseManager;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@RestController
@RequestMapping("danmaku")
public class DanmakuController {
    private final DanmakuService danmakuService;
    private final DanmakuSseManager danmakuSseManager;

    public DanmakuController(DanmakuService danmakuService,
                             DanmakuSseManager danmakuSseManager) {
        this.danmakuService = danmakuService;
        this.danmakuSseManager = danmakuSseManager;
    }

    /**
     * 发送评论
     *
     * @param danmaku 评论
     * @return success
     */
    @ApiOperation("发送弹幕")
    @SaCheckLogin
    @PostMapping("/v3/")
    public DanmakuResponse<DanmakuValueVO> post(@RequestBody @Valid DanmakuDTO danmaku) {
        DanmakuValueVO send = danmakuService.send(danmaku);
        return new DanmakuResponse<DanmakuValueVO>().setCode(0).setData(send);
    }

    /**
     * 获取弹幕
     *
     * @param cid 稿件ID
     * @param max 获取最大数量
     * @return 弹幕列表
     */
    @ApiOperation("获取弹幕")
    @GetMapping(value = "/v3/")
    public DanmakuResponse<List<DanmakuValueVO>> get(@RequestParam("id") @NotNull(message = "稿件ID不得为空") Long cid,
                                                     @RequestParam(value = "max", defaultValue = "0")
                                                     @Min(value = 0, message = "最少为0")
                                                     @Max(value = 1000, message = "最大1000") Integer max) {
        List<DanmakuValueVO> danmakus = danmakuService.danmakus(cid, max);
        return new DanmakuResponse<List<DanmakuValueVO>>().setCode(0).setData(danmakus);
    }

    /**
     * SSE监听弹幕实时推送
     *
     * @param cid 稿件ID
     * @return sse
     */
    @ApiOperation("SSE监听弹幕实时推送")
    @GetMapping(value = "/subscription/{cid}")
    public SseEmitter subscription(@PathVariable("cid") @NotNull(message = "稿件ID") Long cid) {
        String uid = StpUtil.getLoginId("");
        if (StringUtils.isBlank(uid)) {
            return null;
        }
        return danmakuSseManager.create(cid, uid);
    }
}
