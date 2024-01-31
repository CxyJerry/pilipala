package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.jerry.pilipala.application.dto.CommentDTO;
import com.jerry.pilipala.application.vo.vod.CommentVO;
import com.jerry.pilipala.domain.interactive.entity.CommentInteractiveParam;
import com.jerry.pilipala.domain.interactive.entity.DeleteCommentInteractiveParam;
import com.jerry.pilipala.domain.interactive.handler.InteractiveActionTrigger;
import com.jerry.pilipala.domain.vod.service.CommentService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final InteractiveActionTrigger interactiveActionTrigger;

    public CommentController(CommentService commentService,
                             InteractiveActionTrigger interactiveActionTrigger) {
        this.commentService = commentService;

        this.interactiveActionTrigger = interactiveActionTrigger;
    }

    /**
     * 发送评论
     *
     * @param commentDTO dto
     * @return success
     */
    @Operation(summary = "发送评论")
    @SaCheckLogin
    @PostMapping("/post")
    public CommonResponse<?> post(@RequestBody @Valid CommentDTO commentDTO) {
        String uid = (String) StpUtil.getLoginId();
        CommentInteractiveParam param = new CommentInteractiveParam();
        param.setComment(commentDTO)
                .setCid(Long.valueOf(commentDTO.getCid()))
                .setSelfUid(uid);

        interactiveActionTrigger.trigger(VodInteractiveActionEnum.COMMENT, param);
        return CommonResponse.success();
    }

    @Operation(summary = "删除评论")
    @SaCheckLogin
    @DeleteMapping("/delete")
    public CommonResponse<?> delete(@RequestParam(value = "cid")
                                    @NotBlank(message = "cid 不得为空") String cid,
                                    @RequestParam(value = "comment_id")
                                    @NotBlank(message = "评论ID不得为空") String commentId) {
        DeleteCommentInteractiveParam param = new DeleteCommentInteractiveParam();
        param.setCommentId(commentId)
                .setCid(Long.valueOf(cid))
                .setSelfUid((String) StpUtil.getLoginId());
        interactiveActionTrigger.trigger(VodInteractiveActionEnum.DELETE_COMMENT, param);
        return CommonResponse.success();
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
    @Operation(summary = "获取评论")
    @GetMapping("/get")
    public CommonResponse<?> get(@RequestParam("cid") @NotBlank(message = "稿件ID不得为空") String cid,
                                 @RequestParam("parent_comment_id") String commentId,
                                 @RequestParam(value = "page_no", defaultValue = "1") Integer pageNo,
                                 @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Page<CommentVO> commentPage = commentService.get(cid, commentId, pageNo, pageSize);
        return CommonResponse.success(commentPage);
    }
}
