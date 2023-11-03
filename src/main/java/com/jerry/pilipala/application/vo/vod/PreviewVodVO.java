package com.jerry.pilipala.application.vo.vod;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PreviewVodVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long cid;
    private String name;
    private String url;
}
