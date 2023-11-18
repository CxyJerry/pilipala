package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.dto.EmailLoginDTO;
import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;

    }

    /**
     * 获取登录验证码
     *
     * @param tel 手机号
     * @return success
     */
    @ApiOperation("获取登录验证码")
    @GetMapping("/code")
    public CommonResponse<?> code(@RequestParam("tel") @NotBlank(message = "手机号不得为空") String tel) {
        userService.code(tel);
        return CommonResponse.success();
    }

    /**
     * 登录
     *
     * @param loginDTO dto
     * @return userVO
     */
    @ApiOperation("登录")
    @PostMapping("/login")
    @RateLimiter(key = "user-limit:login", seconds = 3, count = 1, limitType = LimitType.IP)
    public CommonResponse<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO);
        return CommonResponse.success(userVO);
    }

    /**
     * 获取邮箱登录验证码
     *
     * @param email 手机号
     * @return success
     */
    @ApiOperation("获取邮箱登录验证码")
    @GetMapping("/email-code")
    public CommonResponse<?> emailCode(@RequestParam("email") @NotBlank(message = "邮箱不得为空") String email) {
        userService.emailCode(email);
        return CommonResponse.success();
    }

    /**
     * 登录
     *
     * @param loginDTO dto
     * @return userVO
     */
    @ApiOperation("邮箱验证码登录")
    @PostMapping("/email-login")
    @RateLimiter(key = "user-limit:login", seconds = 3, count = 1, limitType = LimitType.IP)
    public CommonResponse<?> emailLogin(@Valid @RequestBody EmailLoginDTO loginDTO) {
        UserVO userVO = userService.emailLogin(loginDTO);
        return CommonResponse.success(userVO);
    }

    /**
     * 登出
     */
    @ApiOperation("登出")
    @GetMapping("/logout")
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取用户基本信息
     *
     * @param uid 用户ID
     * @return userVO
     */
    @ApiOperation("获取用户基本信息")
    @GetMapping("/info")
    public CommonResponse<?> info(@RequestParam("uid") String uid) {
        UserVO userVO = userService.userVO(uid, false);
        return CommonResponse.success(userVO);
    }

    /**
     * 获取用户列表
     *
     * @param pageNo   页码
     * @param pageSize 数量
     * @return page
     */
    @ApiOperation("获取用户列表")
    @GetMapping("/page")
    public CommonResponse<?> page(@RequestParam("page_no")
                                  @Min(value = 1, message = "最小1")
                                  @Max(value = 1000, message = "最大1000") Integer pageNo,
                                  @RequestParam("page_size")
                                  @Min(value = 1, message = "最小1")
                                  @Max(value = 1000, message = "最大1000") Integer pageSize) {
        Page<UserVO> page = userService.page(pageNo, pageSize);
        return CommonResponse.success(page);
    }

}
