package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.dto.RoleSaveDTO;
import com.jerry.pilipala.application.vo.user.PermissionVO;
import com.jerry.pilipala.application.vo.user.RoleVO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.domain.user.entity.mongo.Apply;
import com.jerry.pilipala.domain.user.entity.mongo.Path;
import com.jerry.pilipala.domain.user.entity.mongo.Permission;
import com.jerry.pilipala.domain.user.entity.mongo.Role;
import com.jerry.pilipala.domain.user.service.PermissionService;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.annotations.Api;
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


    public UserController(UserService userService) {
        this.userService = userService;

    }

    @ApiOperation("获取登录验证码")
    @GetMapping("/code")
    @RateLimiter(key = "user-limit:code", seconds = 60, count = 1, message = "验证码已发送", limitType = LimitType.IP)
    public CommonResponse<?> code(@RequestParam("tel") String tel) {
        userService.code(tel);
        return CommonResponse.success();
    }

    @ApiOperation("登录")
    @PostMapping("/login")
    @RateLimiter(key = "user-limit:login", seconds = 3, count = 1, limitType = LimitType.IP)
    public CommonResponse<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO);
        return CommonResponse.success(userVO);
    }

    @ApiOperation("登出")
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

    @ApiOperation("获取用户列表")
    @GetMapping("/page")
    public CommonResponse<?> page(@RequestParam("page_no") Integer pageNo,
                                  @RequestParam("page_size") Integer pageSize) {
        Page<UserVO> page = userService.page(pageNo, pageSize);
        return CommonResponse.success(page);
    }

}
