package com.jerry.pilipala.application.vo.vod;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InteractionInfoVO {
    private Long cid;
    private Boolean liked = false;
    private Boolean coined = false;
    private Boolean collected = false;
}
