package com.jerry.pilipala.infrastructure.enums.redis;

public interface VodCacheKeyEnum {
    interface StringKey {

    }

    interface HashKey {

        String VOD_INFO_CACHE_KEY = "vod-statistics-cache";
        String PLAY_OFFSET_CACHE_KEY = "vod-play-offset-cache-";

    }

    interface SetKey {
        String LIKE_SET = "like-set-";
        String COIN_SET = "coin-set-";
        String COLLECT_SET = "collect-set-";
        String OFTEN_INTERACTIVE_SET = "often_interactive-";
    }

    interface StreamKey {
        String INTERACTIVE = "interactive";
        String PLAY_ACTION = "play-action";
    }

}
