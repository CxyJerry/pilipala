package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.jerry.pilipala.application.dto.RoleSaveDTO;
import com.jerry.pilipala.application.vo.user.ApplyVO;
import com.jerry.pilipala.application.vo.user.PathVO;
import com.jerry.pilipala.application.vo.user.PermissionVO;
import com.jerry.pilipala.application.vo.user.RoleVO;
import com.jerry.pilipala.domain.user.entity.mongo.Apply;
import com.jerry.pilipala.domain.user.service.PermissionService;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/manage")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @ApiOperation("用户权限授予")
    @SaCheckPermission("permission-manage")
    @PutMapping("/role/grant")
    public CommonResponse<?> grantRole(@RequestParam("apply_id") String applyId,
                                       @RequestParam("uid") String uid,
                                       @RequestParam("role_id") String roleId) {
        permissionService.grantRole(applyId, uid, roleId);
        return CommonResponse.success();
    }

    @ApiOperation("权限创建")
    @SaCheckPermission("permission-manage")
    @PostMapping("/permission/create")
    public CommonResponse<?> createPermission(@RequestParam("name") String name,
                                              @RequestParam("value") String value) {
        PermissionVO permission = permissionService.createPermission(name, value);
        return CommonResponse.success(permission);
    }

    @ApiOperation("删除权限")
    @SaCheckPermission("permission-manage")
    @DeleteMapping("/permission/delete")
    public CommonResponse<?> deletePermission(@RequestParam("permission_id") String permissionId) {
        permissionService.deletePermission(permissionId);
        return CommonResponse.success();
    }

    @ApiOperation("权限列表")
    @SaCheckPermission("permission-manage")
    @GetMapping("/permission/list")
    public CommonResponse<?> permissionList() {
        List<PermissionVO> permissions = permissionService.permissions();
        return CommonResponse.success(permissions);
    }

    @ApiOperation("角色列表")
    @SaCheckPermission("permission-manage")
    @GetMapping("/role/list")
    public CommonResponse<?> roleList() {
        List<RoleVO> roles = permissionService.roles();
        return CommonResponse.success(roles);
    }

    @ApiOperation("角色创建")
    @SaCheckPermission("permission-manage")
    @PutMapping("/role/save")
    public CommonResponse<?> saveRole(@RequestBody @Valid RoleSaveDTO roleSaveDTO) {
        RoleVO roleVO = permissionService.saveRole(
                roleSaveDTO.getId(),
                roleSaveDTO.getName(),
                roleSaveDTO.getPermissionIds()
        );
        return CommonResponse.success(roleVO);
    }

    @ApiOperation("角色删除")
    @SaCheckPermission("permission-manage")
    @DeleteMapping("/role/delete")
    public CommonResponse<?> deleteRole(@RequestParam(value = "role_id") String roleId) {
        permissionService.deleteRole(roleId);
        return CommonResponse.success();
    }

    @ApiOperation("权限申请")
    @SaCheckLogin
    @PostMapping("/permission/apply")
    public CommonResponse<?> permissionApply() {
        permissionService.permissionApply();
        return CommonResponse.success();
    }

    @ApiOperation("权限申请列表")
    @SaCheckPermission("permission-manage")
    @GetMapping("/permission/apply/page")
    public CommonResponse<?> applyPage(@RequestParam("page_no") Integer pageNo,
                                       @RequestParam("page_size") Integer pageSize,
                                       @RequestParam("status") String status) {
        Page<ApplyVO> page = permissionService.applyPage(pageNo, pageSize, status);
        return CommonResponse.success(page);
    }

    @ApiOperation("访问路径创建")
    @SaCheckPermission("permission-manage")
    @PutMapping("/path/save")
    public CommonResponse<?> savePath(@RequestParam("path") String path,
                                      @RequestParam(value = "permission_id", required = false, defaultValue = "") String permissionId) {
        PathVO pathVO = permissionService.savePath(path, permissionId);
        return CommonResponse.success(pathVO);
    }

    @ApiOperation("访问路径列表")
    @SaCheckPermission("permission-manage")
    @GetMapping("/path/get")
    public CommonResponse<?> pathPage() {
        List<PathVO> page = permissionService.paths();
        return CommonResponse.success(page);
    }

    @ApiOperation("删除路径")
    @SaCheckPermission("permission-manage")
    @DeleteMapping("/path/delete")
    public CommonResponse<?> deletePath(@RequestParam("path_id") String pathId) {
        permissionService.deletePath(pathId);
        return CommonResponse.success();
    }

    @ApiOperation("可访问的路径列表")
    @GetMapping("/path/accessible")
    public CommonResponse<?> accessiblePath() {
        List<String> paths = permissionService.accessiblePath();
        return CommonResponse.success(paths);
    }
}
