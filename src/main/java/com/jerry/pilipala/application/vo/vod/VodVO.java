package com.jerry.pilipala.application.vo.vod;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Accessors(chain = true)
public class VodVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long cid;
    private String bvId;
    private String coverUrl;
    private String title;
    private String gcType;
    private String partition;
    private String subPartition;
    private List<String> labels;
    private String desc;
    private Integer offset = 0;
    private Integer viewCount = 0;
    private Integer likeCount = 0;
    private Integer barrageCount = 0;
    private Integer commentCount = 0;
    private Integer coinCount = 0;
    private Integer collectCount = 0;
    private Integer shareCount = 0;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long likeTime = 0L;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long coinTime = 0L;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long collectTime = 0L;
    private List<QualityVO> quality;
    private Long onlineCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long mtime = System.currentTimeMillis();
}
