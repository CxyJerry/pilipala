package com.jerry.pilipala.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PreUploadVO {
    private String bvId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long cid;
    private String filename;
}
