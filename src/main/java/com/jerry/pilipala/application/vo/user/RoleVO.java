package com.jerry.pilipala.application.vo.user;

import com.jerry.pilipala.domain.user.entity.mongo.Permission;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RoleVO {
    private String id;
    private String name;
    private List<Permission> permissions;
    private Long ctime = System.currentTimeMillis();
}
