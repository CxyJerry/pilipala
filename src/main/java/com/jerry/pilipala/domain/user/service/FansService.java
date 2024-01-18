package com.jerry.pilipala.domain.user.service;

import com.jerry.pilipala.application.vo.user.DynamicVO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.infrastructure.utils.Page;

import java.util.List;

public interface FansService {
    UserVO put(String upUid);

    List<UserVO> idles();

    Page<DynamicVO> dynamic(String uid, Integer pageNo, Integer pageSize);
}
