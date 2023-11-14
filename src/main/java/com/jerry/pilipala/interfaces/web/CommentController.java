package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jerry.pilipala.application.dto.CommentDTO;
import com.jerry.pilipala.application.vo.vod.CommentVO;
import com.jerry.pilipala.domain.vod.service.CommentService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.utils.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
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
