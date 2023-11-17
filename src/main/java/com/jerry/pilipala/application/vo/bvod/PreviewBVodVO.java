package com.jerry.pilipala.application.vo.bvod;

import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import com.jerry.pilipala.application.vo.vod.PreviewVodVO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PreviewBVodVO {
    private String bvId;
    private String coverUrl;
    private String title;
    private String desc;
    private String partition;
    private Integer viewCount;
    private Integer likeCount;
    private Integer barrageCount;
    private Integer commentCount;
    private Integer coinCount;
    private Integer collectCount;
    private Integer shareCount;
    private PreviewUserVO author;
    private PreviewVodVO preview;
    private Double duration;
}
