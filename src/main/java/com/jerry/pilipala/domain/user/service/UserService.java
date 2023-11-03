package com.jerry.pilipala.domain.user.service;

import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.vo.user.UserVO;

import java.util.List;
import java.util.Set;

public interface UserService {
    UserVO login(LoginDTO loginDTO);

    String code(String tel);

    UserVO userVO(String uid);

    List<UserVO> userVoList(Set<String> uidSet);
}
