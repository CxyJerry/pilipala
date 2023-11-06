package com.jerry.pilipala.application.vo;

import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CommentVO {
    private String id;
    private String cid;
    private PreviewUserVO author;
    private String content;
    private Boolean hasChild;
    private Long date;
}
