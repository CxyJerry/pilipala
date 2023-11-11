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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Validated
@RestController
@RequestMapping("/manage")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 用户权限授予
     *
     * @param applyId 申请ID
     * @param uid     用户ID
     * @param roleId  角色ID
     * @return success
     */
    @ApiOperation("用户权限授予")
    @SaCheckPermission("permission-manage")
    @PutMapping("/role/grant")
    public CommonResponse<?> grantRole(@RequestParam("apply_id") @NotBlank(message = "申请ID不得为空") String applyId,
                                       @RequestParam("uid") @NotBlank(message = "用户ID不得为空") String uid,
                                       @RequestParam("role_id") @NotBlank(message = "角色ID不得为空") String roleId) {
        permissionService.grantRole(applyId, uid, roleId);
        return CommonResponse.success();
    }

    /**
     * 权限创建
     *
     * @param name  权限名
     * @param value 权限值
     * @return permission
     */
    @ApiOperation("权限创建")
    @SaCheckPermission("permission-manage")
    @PostMapping("/permission/create")
    public CommonResponse<?> createPermission(@RequestParam("name") @NotBlank(message = "权限名不得为空") String name,
                                              @RequestParam("value") @NotBlank(message = "权限值不得为空") String value) {
        PermissionVO permission = permissionService.createPermission(name, value);
        return CommonResponse.success(permission);
    }

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return success
     */
    @ApiOperation("删除权限")
    @SaCheckPermission("permission-manage")
    @DeleteMapping("/permission/delete")
    public CommonResponse<?> deletePermission(@RequestParam("permission_id")
                                              @NotBlank(message = "权限ID不得为空") String permissionId) {
        permissionService.deletePermission(permissionId);
        return CommonResponse.success();
    }

    /**
     * 权限列表
     *
     * @return list
     */
    @ApiOperation("权限列表")
    @SaCheckPermission("permission-manage")
    @GetMapping("/permission/list")
    public CommonResponse<?> permissionList() {
        List<PermissionVO> permissions = permissionService.permissions();
        return CommonResponse.success(permissions);
    }

    /**
     * 角色列表
     *
     * @return list
     */
    @ApiOperation("角色列表")
    @SaCheckPermission("permission-manage")
    @GetMapping("/role/list")
    public CommonResponse<?> roleList() {
        List<RoleVO> roles = permissionService.roles();
        return CommonResponse.success(roles);
    }

    /**
     * 角色创建
     *
     * @param roleSaveDTO dto
     * @return role
     */
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

    /**
     * 角色删除
     *
     * @param roleId 角色ID
     * @return success
     */
    @ApiOperation("角色删除")
    @SaCheckPermission("permission-manage")
    @DeleteMapping("/role/delete")
    public CommonResponse<?> deleteRole(@RequestParam(value = "role_id")
                                        @NotBlank(message = "角色ID不得为空") String roleId) {
        permissionService.deleteRole(roleId);
        return CommonResponse.success();
    }

    /**
     * 权限申请
     *
     * @return success
     */
    @ApiOperation("权限申请")
    @SaCheckLogin
    @PostMapping("/permission/apply")
    public CommonResponse<?> permissionApply() {
        permissionService.permissionApply();
        return CommonResponse.success();
    }

    /**
     * 权限申请列表
     *
     * @param pageNo   页码
     * @param pageSize 数量
     * @param status   处理状态
     * @return page
     */
    @ApiOperation("权限申请列表")
    @SaCheckPermission("permission-manage")
    @GetMapping("/permission/apply/page")
    public CommonResponse<?> applyPage(@RequestParam("page_no")
                                       @Min(value = 1, message = "最小1")
                                       @Max(value = 1000, message = "最大1000") Integer pageNo,
                                       @RequestParam("page_size")
                                       @Min(value = 1, message = "最小1")
                                       @Max(value = 1000, message = "最大1000") Integer pageSize,
                                       @RequestParam("status") @NotBlank(message = "处理状态不得为空") String status) {
        Page<ApplyVO> page = permissionService.applyPage(pageNo, pageSize, status);
        return CommonResponse.success(page);
    }

    /**
     * 访问路径创建
     *
     * @param path         路径
     * @param permissionId 权限ID(可选）
     * @return path
     */
    @ApiOperation("访问路径创建")
    @SaCheckPermission("permission-manage")
    @PutMapping("/path/save")
    public CommonResponse<?> savePath(@RequestParam("path") @NotBlank(message = "路径不得为空") String path,
                                      @RequestParam(value = "permission_id", required = false, defaultValue = "") String permissionId) {
        PathVO pathVO = permissionService.savePath(path, permissionId);
        return CommonResponse.success(pathVO);
    }

    /**
     * 访问路径列表
     *
     * @return list
     */
    @ApiOperation("访问路径列表")
    @SaCheckPermission("permission-manage")
    @GetMapping("/path/get")
    public CommonResponse<?> pathPage() {
        List<PathVO> page = permissionService.paths();
        return CommonResponse.success(page);
    }

    /**
     * 删除路径
     *
     * @param pathId 路径ID
     * @return success
     */
    @ApiOperation("删除路径")
    @SaCheckPermission("permission-manage")
    @DeleteMapping("/path/delete")
    public CommonResponse<?> deletePath(@RequestParam("path_id")
                                        @NotBlank(message = "路径ID不得为空") String pathId) {
        permissionService.deletePath(pathId);
        return CommonResponse.success();
    }

    /**
     * 可访问的路径列表
     *
     * @return list
     */
    @ApiOperation("可访问的路径列表")
    @GetMapping("/path/accessible")
    public CommonResponse<?> accessiblePath() {
        List<String> paths = permissionService.accessiblePath();
        return CommonResponse.success(paths);
    }
}
