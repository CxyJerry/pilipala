package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.vo.user.DynamicVO;
import com.jerry.pilipala.application.vo.user.UserVO;
import com.jerry.pilipala.domain.interactive.entity.FollowInteractiveParam;
import com.jerry.pilipala.domain.interactive.handler.InteractiveActionTrigger;
import com.jerry.pilipala.domain.user.service.FansService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/fans")
public class FansController {
    private final FansService fansService;
    private final InteractiveActionTrigger interactiveActionTrigger;

    public FansController(FansService fansService,
                          InteractiveActionTrigger interactiveActionTrigger) {
        this.fansService = fansService;
        this.interactiveActionTrigger = interactiveActionTrigger;
    }

    /**
     * 关注/取消关注
     *
     * @param upUid upID
     * @return userVO
     */
    @Operation(summary = "关注/取消关注")
    @SaCheckLogin
    @PutMapping("/put")
    public CommonResponse<?> put(@RequestParam("up_uid") @NotBlank(message = "用户ID不存在") String upUid) {
        FollowInteractiveParam param = new FollowInteractiveParam();
        String uid = (String) StpUtil.getLoginId();
        param.setUpUid(upUid)
                .setSelfUid(uid);

        interactiveActionTrigger.trigger(VodInteractiveActionEnum.FOLLOW, param);
        return CommonResponse.success();
    }

    /**
     * 获取关注列表
     *
     * @return 关注列表
     */
    @Operation(summary = "获取关注列表")
    @SaCheckLogin
    @GetMapping("/idles")
    public CommonResponse<?> idles() {
        List<UserVO> idles = fansService.idles();
        return CommonResponse.success(idles);
    }

    @Operation(summary = "获取动态消息")
    @SaCheckLogin
    @GetMapping("/dynamic")
    public CommonResponse<?> dynamic(
            @RequestParam(value = "uid", required = false, defaultValue = "") String uid,
            @RequestParam(value = "page_no", defaultValue = "1")
            @Min(value = 1, message = "非法页码")
            Integer pageNo,
            @RequestParam(value = "page_size", defaultValue = "10")
            @Min(value = 1, message = "非法数据请求量")
            Integer pageSize) {
        Page<DynamicVO> dynamic = fansService.dynamic(uid, pageNo, pageSize);
        return CommonResponse.success(dynamic);
    }
}
