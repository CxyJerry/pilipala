package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.dto.DanmakuDTO;
import com.jerry.pilipala.application.vo.DanmakuResponse;
import com.jerry.pilipala.application.vo.DanmakuValueVO;
import com.jerry.pilipala.domain.vod.service.DanmakuService;
import com.jerry.pilipala.domain.vod.service.DanmakuSseManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
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

    @PostMapping("/v3/")
    public DanmakuResponse<DanmakuValueVO> post(@RequestBody @Valid DanmakuDTO danmaku) {
        DanmakuValueVO send = danmakuService.send(danmaku);
        return new DanmakuResponse<DanmakuValueVO>().setCode(0).setData(send);
    }

    @GetMapping(value = "/v3/")
    public DanmakuResponse<List<DanmakuValueVO>> get(@RequestParam("id") Long cid,
                                                     @RequestParam(value = "max", defaultValue = "0") Integer max) {
        List<DanmakuValueVO> danmakus = danmakuService.danmakus(cid, max);
        return new DanmakuResponse<List<DanmakuValueVO>>().setCode(0).setData(danmakus);
    }

    @GetMapping(value = "/subscription/{cid}")
    public SseEmitter subscription(@PathVariable("cid") Long cid) {
        String uid = StpUtil.getLoginId("");
        if (StringUtils.isBlank(uid)) {
            return null;
        }
        return danmakuSseManager.create(cid, uid);
    }
}
