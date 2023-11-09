package com.jerry.pilipala.application.vo.vod;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DanmakuVO {
    private String _id;
    private String player;
    private String author;
    private Double time;
    private String text;
    private Integer color;
    private String ip;
    private Long date;
    private Integer __v;
}
