package com.jerry.pilipala.application.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class CommentDTO {
    @NotBlank(message = "稿件id不得为空")
    private String cid;
    private String parentCommentId;
    @NotBlank(message = "评论内容不得为空")
    private String content;
}
