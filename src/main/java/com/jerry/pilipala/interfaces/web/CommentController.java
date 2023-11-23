package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.google.common.collect.Maps;
import com.jerry.pilipala.application.dto.CommentDTO;
import com.jerry.pilipala.application.vo.vod.CommentVO;
import com.jerry.pilipala.domain.vod.service.CommentService;
import com.jerry.pilipala.domain.vod.service.impl.handler.InteractiveActionStrategy;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;

@Validated
@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final InteractiveActionStrategy interactiveActionStrategy;

    public CommentController(CommentService commentService,
                             InteractiveActionStrategy interactiveActionStrategy) {
        this.commentService = commentService;

        this.interactiveActionStrategy = interactiveActionStrategy;
    }

    /**
     * 发送评论
     *
     * @param commentDTO dto
     * @return success
     */
    @ApiOperation("发送评论")
    @SaCheckLogin
    @PostMapping("/post")
    public CommonResponse<?> post(@RequestBody @Valid CommentDTO commentDTO) {
        CommentVO commentVO = commentService.post(commentDTO);

        HashMap<@Nullable String, @Nullable Object> params = Maps.newHashMap();
        params.put("cid", commentDTO.getCid());
        interactiveActionStrategy.trigger(VodInteractiveActionEnum.COMMENT, params);
        return CommonResponse.success(commentVO);
    }

    /**
     * 获取评论（分页）
     *
     * @param cid       稿件ID
     * @param commentId 上一级评论ID
     * @param pageNo    页码
     * @param pageSize  数量
     * @return page
     */
    @ApiOperation("获取评论")
    @GetMapping("/get")
    public CommonResponse<?> get(@RequestParam("cid") @NotBlank(message = "稿件ID不得为空") String cid,
                                 @RequestParam("parent_comment_id") String commentId,
                                 @RequestParam(value = "page_no", defaultValue = "1") Integer pageNo,
                                 @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Page<CommentVO> commentPage = commentService.get(cid, commentId, pageNo, pageSize);
        return CommonResponse.success(commentPage);
    }
}
