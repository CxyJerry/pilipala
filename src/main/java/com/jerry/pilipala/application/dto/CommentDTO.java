package com.jerry.pilipala.application.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CommentDTO {
    private String cid;
    private String parentCommentId;
    private String content;
}
