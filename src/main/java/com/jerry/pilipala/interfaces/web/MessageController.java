package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.vo.message.MessageVO;
import com.jerry.pilipala.domain.message.service.MessageService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/msg")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 获取未读消息数量
     *
     * @return count
     */
    @ApiOperation("获取未读消息数量")
    @GetMapping("/unread/count")
    public CommonResponse<?> unreadCount() {
        String uid = StpUtil.getLoginId("");
        if (StringUtils.isBlank(uid)) {
            return CommonResponse.success(0);
        }
        long unreadCount = messageService.unreadCount(uid);
        return CommonResponse.success(unreadCount);
    }

    /**
     * 分页获取消息
     *
     * @param pageNo   页码
     * @param pageSize 数量
     * @return page
     */
    @ApiOperation("分页获取消息")
    @GetMapping("/page")
    public CommonResponse<?> page(@RequestParam("page_no")
                                  @Min(value = 1, message = "最小1")
                                  @Max(value = 1000, message = "最大1000") Integer pageNo,
                                  @RequestParam("page_size")
                                  @Min(value = 1, message = "最小1")
                                  @Max(value = 1000, message = "最大1000") Integer pageSize) {
        Page<MessageVO> page = messageService.page(pageNo, pageSize);
        return CommonResponse.success(page);
    }

}
