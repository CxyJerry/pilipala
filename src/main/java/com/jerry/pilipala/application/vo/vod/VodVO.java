package com.jerry.pilipala.application.vo.vod;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jerry.pilipala.application.vo.QualityVO;
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
    private List<QualityVO> quality;
    private Long onlineCount;
    private Long mtime = System.currentTimeMillis();
}
