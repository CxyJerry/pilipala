package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.application.vo.user.UserVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
        UserVO userVO = userService.userVO(uid);
        return CommonResponse.success(userVO);
    }
}
