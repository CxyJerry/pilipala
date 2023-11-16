package com.jerry.pilipala.domain.vod.entity.mongo.statitics;

import cn.hutool.core.date.DateUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Document("vod_statistics")
public class VodStatistics {
    @Id
    private ObjectId id;
    private Long cid = 0L;
    private String date = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd");
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

    public void incrementViewCount() {
        this.viewCount += 1;
    }

    public void incrementLikeCount() {
        this.likeCount += 1;
    }

    public void incrementBarrageCount() {
        this.barrageCount += 1;
    }

    public void incrementCommentCount() {
        this.commentCount += 1;
    }

    public void incrementCoinCount() {
        this.coinCount += 1;
    }

    public void incrementCollectCount() {
        this.collectCount += 1;
    }

    public void incrementShareCount() {
        this.shareCount += 1;
    }
}
