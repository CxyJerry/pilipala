package com.jerry.pilipala.domain.user.service;

import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.infrastructure.utils.Page;

import java.util.Collection;
import java.util.List;

public interface UserService {
    UserVO login(LoginDTO loginDTO);

    String code(String tel);

    UserVO userVO(String uid, boolean forceQuery);

    List<UserVO> userVoList(Collection<String> uidSet);

    Page<UserVO> page(Integer pageNo, Integer pageSize);
}
