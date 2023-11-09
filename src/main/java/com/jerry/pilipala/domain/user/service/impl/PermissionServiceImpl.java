package com.jerry.pilipala.domain.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.vo.user.*;
import com.jerry.pilipala.domain.message.service.MessageService;
import com.jerry.pilipala.domain.user.entity.mongo.*;
import com.jerry.pilipala.domain.user.service.PermissionService;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.enums.ApplyStatusEnum;
import com.jerry.pilipala.infrastructure.enums.redis.UserCacheKeyEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final MongoTemplate mongoTemplate;
    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final MessageService messageService;


    public PermissionServiceImpl(MongoTemplate mongoTemplate,
                                 UserService userService,
                                 RedisTemplate<String, Object> redisTemplate,
                                 MessageService messageService) {
        this.mongoTemplate = mongoTemplate;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.messageService = messageService;
    }

    @Override
    public PermissionVO createPermission(String name, String value) {
        Permission permission = new Permission().setName(name).setValue(value);
        permission = mongoTemplate.save(permission);
        return buildPermissionVO(permission);
    }

    private PermissionVO buildPermissionVO(Permission permission) {
        return new PermissionVO().setId(permission.getId().toString())
                .setName(permission.getName())
                .setValue(permission.getValue())
                .setCtime(permission.getCtime());
    }

    @Override
    public List<PermissionVO> permissions() {
        return mongoTemplate.find(
                        new Query(Criteria.where("deleted").is(false)),
                        Permission.class)
                .stream()
                .map(this::buildPermissionVO)
                .toList();
    }

    @Override
    public List<PermissionVO> permissions(String roleId) {
        Role role = mongoTemplate
                .findOne(new Query(Criteria.where("_id").is(new ObjectId(roleId))),
                        Role.class);
        if (Objects.isNull(role)) {
            throw BusinessException.businessError("角色不存在");
        }
        List<ObjectId> permissionIds = role.getPermissionIds()
                .stream().map(ObjectId::new).toList();
        return mongoTemplate.find(new Query(Criteria.where("_id").in(permissionIds)
                                .and("deleted").is(false)),
                        Permission.class)
                .stream()
                .filter(Objects::nonNull)
                .map(this::buildPermissionVO)
                .toList();
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
    public List<RoleVO> roles() {
        return mongoTemplate.find(
                        new Query(Criteria.where("deleted").is(false)), Role.class)
                .stream().map(this::buildRoleVO).toList();
    }

    @Override
    public void permissionApply() {
        String uid = (String) StpUtil.getLoginId();
        if (mongoTemplate.exists(
                new Query(Criteria.where("applicantId").is(uid)
                        .and("status").is(ApplyStatusEnum.WAITING.getStatus())),
                Apply.class)) {
            throw BusinessException.businessError("请勿重复申请");
        }
        Apply apply = new Apply().setApplicantId(uid);
        mongoTemplate.save(apply);
    }

    @Override
    public Page<ApplyVO> applyPage(Integer pageNo, Integer pageSize, String status) {
        ApplyStatusEnum statusEnum = ApplyStatusEnum.parse(status);
        Criteria criteria = Criteria.where("status")
                .is(statusEnum.getStatus());
        List<Apply> list = mongoTemplate.find(
                new Query(criteria)
                        .skip((long) Math.max(0, pageNo - 1) * pageSize)
                        .limit(pageSize),
                Apply.class);
        long total = mongoTemplate.count(new Query(criteria), Apply.class);
        List<ApplyVO> page = list.stream().map(apply -> {
            UserVO applicant = userService.userVO(apply.getApplicantId(), false);
            UserVO processor = new UserVO();
            if (StringUtils.isNotBlank(apply.getProcessorId())) {
                processor = userService.userVO(apply.getProcessorId(), false);
            }

            return new ApplyVO()
                    .setId(apply.getId().toString())
                    .setStatus(apply.getStatus())
                    .setCtime(apply.getCtime())
                    .setApplicant(applicant)
                    .setProcessor(processor);
        }).toList();
        return new Page<ApplyVO>()
                .setPageNo(pageNo)
                .setPageSize(pageSize)
                .setTotal(total)
                .setPage(page);
    }

    @Override
    public PathVO savePath(String path, String permissionId) {
        if (mongoTemplate.exists(new Query(Criteria.where("path").is(path)), Path.class)) {
            throw BusinessException.businessError("该路径已存在");
        }
        Path p = new Path()
                .setPath(path)
                .setDeleted(false)
                .setPermissionId(permissionId);
        p = mongoTemplate.save(p);
        Permission permission = null;
        if (StringUtils.isNotBlank(permissionId)) {
            permission = mongoTemplate.findById(new ObjectId(permissionId), Permission.class);
        }


        return new PathVO().setId(p.getId().toString())
                .setPath(p.getPath())
                .setPermissionName(Objects.isNull(permission) ? "" : permission.getName())
                .setCtime(p.getCtime());
    }

    @Override
    public List<PathVO> paths() {
        Criteria criteria = Criteria.where("deleted")
                .is(false);
        List<Path> pathList = mongoTemplate.find(
                new Query(criteria),
                Path.class);
        List<ObjectId> permissionIdList = pathList.stream()
                .map(Path::getPermissionId)
                .filter(StringUtils::isNotBlank)
                .map(ObjectId::new)
                .toList();
        List<Permission> permissionList = mongoTemplate
                .find(new Query(Criteria.where("_id")
                                .in(permissionIdList)),
                        Permission.class);
        Map<String, Permission> permissionMap = permissionList.stream()
                .collect(Collectors.toMap(p -> p.getId().toString(), p -> p));

        return pathList.stream().map(p -> {
            Permission permission = permissionMap.get(p.getPermissionId());
            return new PathVO().setId(p.getId().toString())
                    .setPath(p.getPath())
                    .setPermissionName(Objects.isNull(permission) ? "" : permission.getName())
                    .setCtime(p.getCtime());
        }).toList();
    }

    @Override
    public void deletePath(String pathId) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(new ObjectId(pathId))),
                new Update().set("deleted", true),
                Path.class);
    }

    @Override
    public void deleteRole(String roleId) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(new ObjectId(roleId))),
                new Update().set("deleted", true),
                Role.class);
    }

    @Override
    public void deletePermission(String permissionId) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(new ObjectId(permissionId))),
                new Update().set("deleted", true),
                Permission.class);
    }

    @Override
    public List<String> accessiblePath() {
        List<String> paths = new ArrayList<>(mongoTemplate.find(
                new Query(Criteria.where("permissionId").is("")
                        .and("deleted").is(false)),
                Path.class).stream().map(Path::getPath).toList());
        String uid = StpUtil.getLoginId("");
        if (StringUtils.isBlank(uid)) {
            return paths;
        }
        User user = mongoTemplate.findById(new ObjectId(uid), User.class);
        if (Objects.isNull(user)) {
            return paths;
        }
        String roleId = user.getRoleId();
        if (StringUtils.isBlank(roleId)) {
            return paths;
        }
        Role role = mongoTemplate.findById(new ObjectId(roleId), Role.class);
        if (Objects.isNull(role)) {
            return paths;
        }
        List<String> permissionIds = role.getPermissionIds();
        List<String> needPermissionPath = mongoTemplate.find(new Query(
                                Criteria.where("permissionId").in(permissionIds)
                                        .and("deleted").is(false)),
                        Path.class)
                .stream()
                .map(Path::getPath)
                .toList();
        paths.addAll(needPermissionPath);
        return paths;
    }

    @Override
    public void grantRole(String applyId, String uid, String roleId) {
        Object selfUid = StpUtil.getLoginId();
        if (selfUid.equals(uid)) {
            throw BusinessException.businessError("禁止修改自己的权限");
        }
        User user = mongoTemplate.findById(new ObjectId(uid), User.class);
        if (Objects.isNull(user)) {
            throw BusinessException.businessError("用户不存在");
        }
        Apply apply = mongoTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(applyId))
                .and("status").is(ApplyStatusEnum.WAITING.getStatus())), Apply.class);
        if (Objects.isNull(apply)) {
            throw BusinessException.businessError("申请不存在");
        }
        apply.setStatus(ApplyStatusEnum.PROCESSED.getStatus())
                .setProcessorId(selfUid.toString());
        mongoTemplate.save(apply);

        Role role = mongoTemplate.findById(new ObjectId(roleId), Role.class);
        if (Objects.isNull(role)) {
            throw BusinessException.businessError("角色不存在");
        }

        user.setRoleId(roleId);
        mongoTemplate.save(user);

        // 推送站内信
        String msg = "亲爱的用户,您申请的权限已通过审核，感谢您的申请，PiliPala祝您使用愉快。";
        messageService.send("", uid, msg);

        redisTemplate.delete(UserCacheKeyEnum.StringKey.USER_CACHE_KEY.concat(uid));
    }

}
