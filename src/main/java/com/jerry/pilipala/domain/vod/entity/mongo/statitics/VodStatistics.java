package com.jerry.pilipala.domain.vod.entity.mongo.statitics;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("vod_statistics")
public class VodStatistics {
    @Id
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long cid = 0L;
    // 播放数
    private Integer viewCount = 0;
    // 点赞数
    private Integer likeCount = 0;
    // 弹幕数
    private Integer barrageCount = 0;
    // 评论数
    private Integer commentCount = 0;
    // 投币数
    private Integer coinCount = 0;
    // 收藏数
    private Integer collectCount = 0;
    // 分享数
    private Integer shareCount = 0;

    public static VodStatistics EMPTY_STATICS = new VodStatistics();
}
