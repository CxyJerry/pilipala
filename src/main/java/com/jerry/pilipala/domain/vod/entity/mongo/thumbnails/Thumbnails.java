package com.jerry.pilipala.domain.vod.entity.mongo.thumbnails;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class Thumbnails {
    private String url;
    private Integer time;
}
