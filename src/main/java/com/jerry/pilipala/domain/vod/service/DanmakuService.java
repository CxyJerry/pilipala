package com.jerry.pilipala.domain.vod.service;

import com.jerry.pilipala.application.dto.DanmakuDTO;
import com.jerry.pilipala.application.vo.DanmakuVO;
import com.jerry.pilipala.application.vo.DanmakuValueVO;

import java.util.List;

public interface DanmakuService {

    DanmakuVO send(DanmakuDTO danmaku);

    List<DanmakuValueVO> danmakus(Long cid,Integer max);

}
