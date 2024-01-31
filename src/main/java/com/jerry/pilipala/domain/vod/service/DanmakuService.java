package com.jerry.pilipala.domain.vod.service;

import com.jerry.pilipala.application.dto.DanmakuDTO;
import com.jerry.pilipala.application.vo.vod.DanmakuValueVO;

import java.util.List;

public interface DanmakuService {

    void send(String uid, DanmakuDTO danmaku);

    List<DanmakuValueVO> danmakus(Long cid, Integer max);

}
