package com.jerry.pilipala.domain.vod.entity.mongo.distribute;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Quality {
    private Integer qn;
    private String saveTo;
    private String ext;
    private String type;
}
