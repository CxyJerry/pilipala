package com.jerry.pilipala.domain.vod.entity.mongo.statitics;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("vod_statics")
public class VodStatics {
    @Id
    private Long cid = 0L;
    private Integer viewCount = 0;
    private Integer likeCount = 0;
    private Integer barrageCount = 0;
    private Integer commentCount = 0;
    private Integer coinCount = 0;
    private Integer collectCount = 0;
    private Integer shareCount = 0;

    public static VodStatics EMPTY_STATICS = new VodStatics();

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
