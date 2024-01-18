package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.dto.EmailLoginDTO;
import com.jerry.pilipala.application.dto.LoginDTO;
import com.jerry.pilipala.application.dto.UserUpdateDTO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.application.vo.vod.VodVO;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
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

    /**
     * 获取最近收藏
     *
     * @param uid    被获取的用户的ID
     * @param offset 集合偏移量
     * @param size   获取数量
     * @return list
     */
    @ApiOperation("获取最近收藏")
    @GetMapping("/collections")
    public CommonResponse<?> collections(@RequestParam(value = "uid", required = false) String uid,
                                         @RequestParam("offset") Integer offset,
                                         @RequestParam("size") Integer size) {
        List<VodVO> vodList = userService.collections(uid, VodCacheKeyEnum.SetKey.COLLECT_SET, offset, size);
        userService.appendCollectTime(uid, vodList);
        return CommonResponse.success(vodList);
    }

    /**
     * 获取最近投币
     *
     * @param uid    被获取的用户ID
     * @param offset 数据偏移量
     * @param size   获取量
     * @return list
     */
    @ApiOperation(("获取最近投币"))
    @GetMapping("/coins")
    public CommonResponse<?> coins(@RequestParam(value = "uid", required = false) String uid,
                                   @RequestParam("offset") Integer offset,
                                   @RequestParam("size") Integer size) {
        List<VodVO> vodList = userService.collections(uid, VodCacheKeyEnum.SetKey.COIN_SET, offset, size);
        userService.appendCoinTime(uid, vodList);
        return CommonResponse.success(vodList);
    }

    /**
     * 获取最近点赞
     *
     * @param uid    被获取的用户ID
     * @param offset 数据偏移量
     * @param size   获取量
     * @return list
     */
    @ApiOperation(("获取最近点赞"))
    @GetMapping("/likes")
    public CommonResponse<?> likes(@RequestParam(value = "uid", required = false) String uid,
                                   @RequestParam("offset") Integer offset,
                                   @RequestParam("size") Integer size) {
        List<VodVO> vodList = userService.collections(uid, VodCacheKeyEnum.SetKey.LIKE_SET, offset, size);
        userService.appendLikeTime(uid, vodList);
        return CommonResponse.success(vodList);
    }

    /**
     * 修改个人公告
     *
     * @param announcement 新公告内容
     * @return 修改后的公告内容
     */
    @ApiOperation("修改个人公告")
    @PutMapping("/announcement")
    public CommonResponse<?> announcement(String announcement) {
        String announcement_content = userService.announcement(announcement);
        return CommonResponse.success(announcement_content);
    }

    /**
     * 修改用户个人信息
     *
     * @param userUpdateDTO 更新实体数据
     * @return 用户视图模型
     */
    @ApiOperation("修改个人信息")
    @PutMapping("/update")
    public CommonResponse<?> update(@RequestBody UserUpdateDTO userUpdateDTO) {
        UserVO userVO = userService.updateUserInfo(userUpdateDTO);
        return CommonResponse.success(userVO);
    }

}
