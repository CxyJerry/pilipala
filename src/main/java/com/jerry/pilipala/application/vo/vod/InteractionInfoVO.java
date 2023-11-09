package com.jerry.pilipala.application.vo.vod;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InteractionInfoVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long cid;
    private Boolean liked = false;
    private Boolean coined = false;
    private Boolean collected = false;
}
