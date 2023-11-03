package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jerry.pilipala.infrastructure.annotations.RateLimiter;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.LimitType;
import com.jerry.pilipala.domain.user.service.FansService;
import com.jerry.pilipala.application.vo.user.UserVO;
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

    @SaCheckLogin
    @RateLimiter(key = "limit:fans-put", seconds = 1, count = 1, limitType = LimitType.IP)
    @PutMapping("/put")
    public CommonResponse<?> put(@RequestParam("up_uid") @NotBlank(message = "用户ID不存在") String upUid,
                                 @RequestParam("relation")@NotNull(message = "关系状态丢失")Integer relation) {
        UserVO myUserVO = fansService.put(upUid,relation);
        return CommonResponse.success(myUserVO);
    }

    @SaCheckLogin
    @RateLimiter(key = "limit:fans-idles", seconds = 1, count = 1, limitType = LimitType.IP)
    @GetMapping("/idles")
    public CommonResponse<?> idles() {
        List<UserVO> idles = fansService.idles();
        return CommonResponse.success(idles);
    }
}
