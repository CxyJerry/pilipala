package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.dto.TemplateDTO;
import com.jerry.pilipala.application.vo.message.MessageVO;
import com.jerry.pilipala.application.vo.message.TemplateVO;
import com.jerry.pilipala.application.vo.message.UnreadMessageCountVO;
import com.jerry.pilipala.domain.message.service.MessageService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/msg")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "保存消息模板")
    @PostMapping("/template/save")
    public CommonResponse<?> saveTemplate(@RequestBody @Valid TemplateDTO templateDTO) {
        messageService.saveMessageTemplate(templateDTO.getName(), templateDTO.getContent());
        return CommonResponse.success();
    }

    @Operation(summary = "删除消息模板")
    @DeleteMapping("/template/delete")
    public CommonResponse<?> deleteTemplate(@RequestParam("name") String name) {
        messageService.deleteMessageTemplate(name);
        return CommonResponse.success();
    }

    @Operation(summary = "消息模板列表")
    @GetMapping("/template/get")
    public CommonResponse<?> getTemplates() {
        List<TemplateVO> templates = messageService.messageTemplates();
        return CommonResponse.success(templates);
    }

    /**
     * 获取未读消息数量
     *
     * @return count
     */
    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread/count")
    public CommonResponse<?> unreadCount() {
        String uid = StpUtil.getLoginId("");
        if (StringUtils.isBlank(uid)) {
            return CommonResponse.success(0);
        }
        List<UnreadMessageCountVO> unreadMessageCountVOList = messageService.unreadCount(uid);
        return CommonResponse.success(unreadMessageCountVOList);
    }

    /**
     * 分页获取消息
     *
     * @param pageNo   页码
     * @param pageSize 数量
     * @return page
     */
    @Operation(summary = "分页获取消息")
    @GetMapping("/page")
    public CommonResponse<?> page(@RequestParam("type")
                                  @NotBlank(message = "消息类型不得为空") String type,
                                  @RequestParam("page_no")
                                  @Min(value = 1, message = "最小1")
                                  @Max(value = 1000, message = "最大1000") Integer pageNo,
                                  @RequestParam("page_size")
                                  @Min(value = 1, message = "最小1")
                                  @Max(value = 1000, message = "最大1000") Integer pageSize) {
        Page<MessageVO> page = messageService.page(type, pageNo, pageSize);
        return CommonResponse.success(page);
    }

}
