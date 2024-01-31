package com.jerry.pilipala.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class CommentDTO {
    @NotBlank(message = "稿件id不得为空")
    private String cid;
    private String parentCommentId;
    @NotBlank(message = "评论内容不得为空")
    private String content;
}
