package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.google.common.collect.Maps;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.domain.interactive.handler.InteractiveActionStrategy;
import com.jerry.pilipala.domain.user.service.FansService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Validated
@RestController
@RequestMapping("/fans")
public class FansController {
    private final FansService fansService;
    private final InteractiveActionStrategy interactiveActionStrategy;

    public FansController(FansService fansService,
                          InteractiveActionStrategy interactiveActionStrategy) {
        this.fansService = fansService;
        this.interactiveActionStrategy = interactiveActionStrategy;
    }

    /**
     * 关注/取消关注
     *
     * @param upUid upID
     * @return userVO
     */
    @ApiOperation("关注/取消关注")
    @SaCheckLogin
    @PutMapping("/put")
    public CommonResponse<?> put(@RequestParam("up_uid") @NotBlank(message = "用户ID不存在") String upUid) {
        UserVO myUserVO = fansService.put(upUid);
        // 新增互动数据
        interactiveActionStrategy.trigger(VodInteractiveActionEnum.FOLLOW, Maps.newHashMap());

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
