package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.dto.DanmakuDTO;
import com.jerry.pilipala.application.vo.vod.DanmakuResponse;
import com.jerry.pilipala.application.vo.vod.DanmakuValueVO;
import com.jerry.pilipala.domain.interactive.entity.BarrageInteractiveParam;
import com.jerry.pilipala.domain.interactive.handler.InteractiveActionTrigger;
import com.jerry.pilipala.domain.vod.service.DanmakuService;
import com.jerry.pilipala.domain.vod.service.DanmakuSseManager;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/danmaku")
public class DanmakuController {
    private final DanmakuService danmakuService;
    private final DanmakuSseManager danmakuSseManager;
    private final InteractiveActionTrigger interactiveActionTrigger;

    public DanmakuController(DanmakuService danmakuService,
                             DanmakuSseManager danmakuSseManager,
                             InteractiveActionTrigger interactiveActionTrigger) {
        this.danmakuService = danmakuService;
        this.danmakuSseManager = danmakuSseManager;
        this.interactiveActionTrigger = interactiveActionTrigger;
    }

    /**
     * 发送弹幕
     *
     * @param danmaku 弹幕
     * @return success
     */
    @Operation(summary = "发送弹幕")
    @SaCheckLogin
    @PostMapping("/v3/")
    public CommonResponse<?> post(@RequestBody @Valid DanmakuDTO danmaku) {
        String uid = (String) StpUtil.getLoginId();
        BarrageInteractiveParam param = new BarrageInteractiveParam()
                .setCid(danmaku.getId())
                .setDanmaku(danmaku);
        param.setSelfUid(uid);
        interactiveActionTrigger.trigger(VodInteractiveActionEnum.BARRAGE, param);

        return CommonResponse.success();
    }

    /**
     * 获取弹幕
     *
     * @param cid 稿件ID
     * @param max 获取最大数量
     * @return 弹幕列表
     */
    @Operation(summary = "获取弹幕")
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
    @Operation(summary = "SSE监听弹幕实时推送")
    @GetMapping(value = "/subscription/{cid}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscription(HttpServletResponse response,
                                   @PathVariable("cid") @NotNull(message = "稿件ID") Long cid) {
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        String uid = StpUtil.getLoginId("");
        if (StringUtils.isBlank(uid)) {
            log.info("未登录，无需建立SSE连接");
            return null;
        }
        return danmakuSseManager.create(cid, uid);
    }
}
