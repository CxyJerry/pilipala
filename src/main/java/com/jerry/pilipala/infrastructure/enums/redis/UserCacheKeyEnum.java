package com.jerry.pilipala.infrastructure.enums.redis;

public interface UserCacheKeyEnum {
    interface StringKey {
        String USER_ENTITY_KEY = "user-entity-";
    }

    interface HashKey {
        String USER_VO_HASH_KEY = "user-vo-hash";
    }

    interface SetKey {
        String COLLECT_VOD_SET = "collect-";
    }

}
