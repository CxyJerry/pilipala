package com.jerry.pilipala.domain.user.service;

import com.jerry.pilipala.application.dto.EmailLoginDTO;
import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.application.vo.vod.VodVO;
import com.jerry.pilipala.infrastructure.utils.Page;

import java.util.Collection;
import java.util.List;

public interface UserService {
    UserVO login(LoginDTO loginDTO);

    void code(String tel);

    UserVO userVO(String uid, boolean forceQuery);

    List<UserVO> userVoList(Collection<String> uidSet);

    Page<UserVO> page(Integer pageNo, Integer pageSize);

    void emailCode(String email);

    UserVO emailLogin(EmailLoginDTO loginDTO);

    List<VodVO> collections(String uid, String setKey, Integer offset, Integer size);

    void appendCollectTime(String uid, List<VodVO> vodVOList);

    void appendCoinTime(String uid, List<VodVO> vodVOList);

    void appendLikeTime(String uid, List<VodVO> vodVOList);

    String announcement(String announcement);
}
