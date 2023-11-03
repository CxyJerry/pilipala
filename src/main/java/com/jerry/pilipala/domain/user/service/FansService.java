package com.jerry.pilipala.domain.user.service;

import com.jerry.pilipala.application.vo.user.UserVO;

import java.util.List;

public interface FansService {
    UserVO put(String upUid, Integer relation);

    List<UserVO> idles();

}
