package com.jerry.pilipala.domain.user.service;

import com.jerry.pilipala.application.vo.user.RoleVO;
import com.jerry.pilipala.domain.user.entity.mongo.Permission;
import com.jerry.pilipala.domain.user.entity.mongo.Role;

import java.util.List;

public interface PermissionService {
    Permission createPermission(String name, String value);

    List<Permission> permissions();

    List<Permission> permissions(String roleId);

    RoleVO saveRole(String id, String name, List<String> permissionIdList);

    List<Role> roles();
}
