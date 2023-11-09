package com.jerry.pilipala.infrastructure.enums.redis;

public interface UserCacheKeyEnum {
    interface StringKey {
        String USER_CACHE_KEY = "user-entity-";
    }

    interface HashKey {
        String USER_VO_HASH_KEY = "user-vo-hash";
        String ROLE_CACHE_KEY = "role-hash";
        String PERMISSION_CACHE_KEY = "permission-hash";
    }

    interface SetKey {
        String COLLECT_VOD_SET = "collect-";
    }

}
