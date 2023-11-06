package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.vo.user.RoleVO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.domain.user.entity.mongo.Permission;
import com.jerry.pilipala.domain.user.service.PermissionService;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final PermissionService permissionService;

    public UserController(UserService userService, PermissionService permissionService) {
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @GetMapping("/code")
    @RateLimiter(key = "user-limit:code", seconds = 60, count = 1, message = "验证码已发送", limitType = LimitType.IP)
    public CommonResponse<?> code(@RequestParam("tel") String tel) {
        userService.code(tel);
        return CommonResponse.success();
    }

    @PostMapping("/login")
    @RateLimiter(key = "user-limit:login", seconds = 3, count = 1, limitType = LimitType.IP)
    public CommonResponse<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO);
        return CommonResponse.success(userVO);
    }

    @GetMapping("/logout")
    public void logout() {
        StpUtil.logout();
    }

    @ApiOperation("获取用户基本信息")
    @GetMapping("/info")
    public CommonResponse<?> info(@RequestParam("uid") String uid) {
        UserVO userVO = userService.userVO(uid, false);
        return CommonResponse.success(userVO);
    }

    @PutMapping("/role/grant")
    public CommonResponse<?> grantRole(@RequestParam("uid") String uid,
                                       @RequestParam("role_id") String roleId) {
        userService.grantRole(uid, roleId);
        return CommonResponse.success();
    }

    @PostMapping("/permission/create")
    public CommonResponse<?> createPermission(@RequestParam("name") String name,
                                              @RequestParam("value") String value) {
        Permission permission = permissionService.createPermission(name, value);
        return CommonResponse.success(permission);
    }

    @PutMapping("/role/save")
    public CommonResponse<?> saveRole(@RequestParam(value = "role_id", required = false) String id,
                                      @RequestParam("role_name") String name,
                                      @RequestParam("permission_list") List<String> permissionIds) {
        RoleVO roleVO = permissionService.saveRole(id, name, permissionIds);
        return CommonResponse.success(roleVO);
    }

}
