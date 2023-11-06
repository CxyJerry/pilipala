package com.jerry.pilipala.domain.user.service.impl;

import com.jerry.pilipala.application.vo.user.RoleVO;
import com.jerry.pilipala.domain.user.entity.mongo.Permission;
import com.jerry.pilipala.domain.user.entity.mongo.Role;
import com.jerry.pilipala.domain.user.service.PermissionService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final MongoTemplate mongoTemplate;

    public PermissionServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Permission createPermission(String name, String value) {
        Permission permission = new Permission().setName(name).setValue(value);
        permission = mongoTemplate.save(permission);
        return permission;
    }

    @Override
    public List<Permission> permissions() {
        return mongoTemplate.findAll(Permission.class);
    }

    @Override
    public List<Permission> permissions(String roleId) {
        Role role = mongoTemplate
                .findOne(new Query(Criteria.where("_id").is(new ObjectId(roleId))),
                        Role.class);
        if (Objects.isNull(role)) {
            throw BusinessException.businessError("角色不存在");
        }
        List<ObjectId> permissionIds = role.getPermissionIds()
                .stream().map(ObjectId::new).toList();
        return mongoTemplate.find(new Query(Criteria.where("_id").in(permissionIds)),
                Permission.class);
    }

    @Override
    public RoleVO saveRole(String id, String name, List<String> permissionIdList) {
        Role role = null;
        if (StringUtils.isNotBlank(id)) {
            role = mongoTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(id))), Role.class);
        }
        if (Objects.isNull(role)) {
            role = new Role().setName(name).setPermissionIds(permissionIdList);
        }

        if (mongoTemplate.exists(new Query(Criteria.where("name").is(name)), Role.class)) {
            throw BusinessException.businessError("同名角色已存在");
        }
        role = mongoTemplate.save(role);

        return buildRoleVO(role);
    }


    private RoleVO buildRoleVO(Role role) {
        Set<ObjectId> permissionIdSet =
                role.getPermissionIds().stream().map(ObjectId::new).collect(Collectors.toSet());
        List<Permission> permissionList = mongoTemplate.find(
                new Query(Criteria.where("_id").in(permissionIdSet)),
                Permission.class);
        return new RoleVO().setId(role.getId().toString())
                .setName(role.getName())
                .setPermissions(permissionList)
                .setCtime(role.getCtime());
    }

    @Override
    public List<Role> roles() {
        return mongoTemplate.findAll(Role.class);
    }
}
