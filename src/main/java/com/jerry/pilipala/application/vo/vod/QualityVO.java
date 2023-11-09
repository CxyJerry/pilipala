package com.jerry.pilipala.application.vo.vod;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class QualityVO {
    private String name;
    private String url;
}
