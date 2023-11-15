package com.jerry.pilipala.domain.user.service;

import com.jerry.pilipala.application.vo.user.ApplyVO;
import com.jerry.pilipala.application.vo.user.PathVO;
import com.jerry.pilipala.application.vo.user.PermissionVO;
import com.jerry.pilipala.application.vo.user.RoleVO;
import com.jerry.pilipala.infrastructure.utils.Page;

import java.util.List;

public interface PermissionService {
    PermissionVO createPermission(String name, String value);

    List<PermissionVO> permissions();

    List<PermissionVO> permissions(String roleId);

    RoleVO saveRole(String id, String name, List<String> permissionIdList);

    List<RoleVO> roles();

    void permissionApply();

    Page<ApplyVO> applyPage(Integer pageNo, Integer pageSize, String status);

    PathVO savePath(String path, String permissionId);

    List<PathVO> paths();

    void deletePath(String pathId);

    List<String> accessiblePath();

    void deleteRole(String roleId);

    void deletePermission(String permissionId);

    void grantRole(String applyId,String uid, String roleId);
}
