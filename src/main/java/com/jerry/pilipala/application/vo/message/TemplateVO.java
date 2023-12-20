package com.jerry.pilipala.application.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TemplateVO {
    private String name;
    private String content;
    private String authorId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ctime;
}
