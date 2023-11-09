package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.vo.message.MessageVO;
import com.jerry.pilipala.domain.message.service.MessageService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/msg")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/unread/count")
    public CommonResponse<?> unreadCount() {
        String uid = StpUtil.getLoginId("");
        if (StringUtils.isBlank(uid)) {
            return CommonResponse.success(0);
        }
        long unreadCount = messageService.unreadCount(uid);
        return CommonResponse.success(unreadCount);
    }

    @GetMapping("/page")
    public CommonResponse<?> page(@RequestParam("page_no") Integer pageNo,
                                  @RequestParam("page_size") Integer pageSize) {
        Page<MessageVO> page = messageService.page(pageNo, pageSize);
        return CommonResponse.success(page);
    }

}
