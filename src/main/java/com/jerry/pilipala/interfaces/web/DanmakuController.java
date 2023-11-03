package com.jerry.pilipala.interfaces.web;

import com.jerry.pilipala.application.dto.DanmakuDTO;
import com.jerry.pilipala.domain.vod.service.DanmakuService;
import com.jerry.pilipala.application.vo.DanmakuResponse;
import com.jerry.pilipala.application.vo.DanmakuVO;
import com.jerry.pilipala.application.vo.DanmakuValueVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("danmaku")
public class DanmakuController {
    private final DanmakuService danmakuService;

    public DanmakuController(DanmakuService danmakuService) {
        this.danmakuService = danmakuService;
    }

    @PostMapping("/v3/")
    public DanmakuResponse<DanmakuVO> post(@RequestBody @Valid DanmakuDTO danmaku) {
        DanmakuVO send = danmakuService.send(danmaku);
        return new DanmakuResponse<DanmakuVO>().setCode(0).setData(send);
    }

    @GetMapping(value = "/v3/")
    public DanmakuResponse<List<DanmakuValueVO>> get(@RequestParam("id") Long cid,
                                                     @RequestParam(value = "max", defaultValue = "0") Integer max) {
        List<DanmakuValueVO> danmakus = danmakuService.danmakus(cid, max);

        return new DanmakuResponse<List<DanmakuValueVO>>().setCode(0).setData(danmakus);
    }
}
