package com.jerry.pilipala.application.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MessageVO {
    private String id;
    private PreviewUserVO sender;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ctime;
}
