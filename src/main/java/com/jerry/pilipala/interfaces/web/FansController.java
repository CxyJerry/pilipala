package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.domain.user.service.FansService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@RestController
@RequestMapping("/fans")
public class FansController {
    private final FansService fansService;

    public FansController(FansService fansService) {
        this.fansService = fansService;
    }

    /**
     * 关注/取消关注
     *
     * @param upUid    upID
     * @param relation 关注状态
     * @return userVO
     */
    @ApiOperation("关注/取消关注")
    @SaCheckLogin
    @PutMapping("/put")
    public CommonResponse<?> put(@RequestParam("up_uid") @NotBlank(message = "用户ID不存在") String upUid,
                                 @RequestParam("relation") @NotNull(message = "关系状态丢失") Integer relation) {
        UserVO myUserVO = fansService.put(upUid, relation);
        return CommonResponse.success(myUserVO);
    }

    /**
     * 获取关注列表
     *
     * @return 关注列表
     */
    @ApiOperation("获取关注列表")
    @SaCheckLogin
    @GetMapping("/idles")
    public CommonResponse<?> idles() {
        List<UserVO> idles = fansService.idles();
        return CommonResponse.success(idles);
    }
}
