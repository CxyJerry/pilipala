package com.jerry.pilipala.infrastructure.enums.redis;

public interface VodCacheKeyEnum {
    interface StringKey {

    }

    interface HashKey {

        String VOD_INFO_CACHE_KEY = "vod-statistics-cache";

    }

    interface SetKey {
        String LIKE_SET = "like-set-";
        String COIN_SET = "coin-set-";
    }

}
