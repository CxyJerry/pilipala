package com.jerry.pilipala.domain.interactive.entity;

import com.jerry.pilipala.application.dto.CommentDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CommentInteractiveParam extends BaseInteractiveParam {
    private Long cid;
    private CommentDTO comment;
}
