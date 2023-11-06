package com.jerry.pilipala.domain.user.service;

import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.vo.user.UserVO;

import java.util.Collection;
import java.util.List;

public interface UserService {
    UserVO login(LoginDTO loginDTO);

    String code(String tel);

    UserVO userVO(String uid, boolean forceQuery);

    List<UserVO> userVoList(Collection<String> uidSet);

    void grantRole(String uid, String roleId);
}
