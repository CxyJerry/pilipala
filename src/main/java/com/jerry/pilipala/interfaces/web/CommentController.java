package com.jerry.pilipala.interfaces.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jerry.pilipala.application.dto.CommentDTO;
import com.jerry.pilipala.application.vo.vod.CommentVO;
import com.jerry.pilipala.domain.vod.service.CommentService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @SaCheckLogin
    @PostMapping("/post")
    public CommonResponse<?> post(@RequestBody CommentDTO commentDTO) {
        CommentVO commentVO = commentService.post(commentDTO);
        return CommonResponse.success(commentVO);
    }

    @GetMapping("/get")
    public CommonResponse<?> get(@RequestParam("cid") String cid,
                                 @RequestParam("parent_comment_id") String commentId,
                                 @RequestParam("page_no") Integer pageNo,
                                 @RequestParam("page_size") Integer pageSize) {
        Page<CommentVO> commentPage = commentService.get(cid, commentId, pageNo, pageSize);
        return CommonResponse.success(commentPage);
    }
}
