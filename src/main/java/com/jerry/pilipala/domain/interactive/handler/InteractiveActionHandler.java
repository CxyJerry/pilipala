package com.jerry.pilipala.domain.interactive.handler;

import com.jerry.pilipala.domain.interactive.entity.BaseInteractiveParam;
import com.jerry.pilipala.domain.vod.entity.mongo.interactive.VodInteractiveAction;
import com.jerry.pilipala.domain.vod.entity.mongo.statitics.VodStatistics;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.enums.video.VodInteractiveActionEnum;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;
import java.util.UUID;

public abstract class InteractiveActionHandler {
    protected final MongoTemplate mongoTemplate;
    protected final RedisTemplate<String, Object> redisTemplate;

    protected InteractiveActionHandler(MongoTemplate mongoTemplate,
                                       RedisTemplate<String, Object> redisTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
    }

    public VodInteractiveAction handle(BaseInteractiveParam interactiveParam) {
        String uid = interactiveParam.getSelfUid();
        long current = System.currentTimeMillis();
        String id = UUID.randomUUID().toString().replace("-", "");
        VodInteractiveAction interactiveAction = new VodInteractiveAction()
                .setId(id)
                .setUid(uid)
                .setParam(interactiveParam)
                .setInteractiveAction(action().getName())
                .setCtime(current)
                .setMtime(current);
        interactiveAction = mongoTemplate.save(interactiveAction);
        return interactiveAction;
    }


    protected void checkVodStatisticsExists(String cid) {
        VodStatistics vodStatistics;
        synchronized (this) {
            if (!redisTemplate.opsForHash().hasKey(VodCacheKeyEnum.HashKey.VOD_INFO_CACHE_KEY, cid)) {
                vodStatistics = mongoTemplate.findById(cid, VodStatistics.class);
                if (Objects.isNull(vodStatistics)) {
                    vodStatistics = new VodStatistics();
                    vodStatistics.setCid(Long.parseLong(cid));
                }
                redisTemplate.opsForHash().put(VodCacheKeyEnum.HashKey.VOD_INFO_CACHE_KEY, cid, vodStatistics);
            }
        }
    }

    protected void incVodStatistics(String cid, String field, boolean inc) {
        String script = """
                local bigKey = KEYS[1]
                local cid = ARGV[1]
                local field = ARGV[2]
                local inc = ARGV[3] == "true"
                local object = redis.call('HGET', bigKey, cid)
                if object then
                    object = cjson.decode(object)
                    if object[field] then
                        if inc then
                            object[field] = object[field] + 1
                        else
                            object[field] = object[field] - 1
                        end
                        redis.call('HSET', bigKey, cid, cjson.encode(object))
                    end
                end
                """;

        redisTemplate.execute(connection -> {
            connection.eval(
                    script.getBytes(),
                    ReturnType.VALUE,
                    1,
                    VodCacheKeyEnum.HashKey.VOD_INFO_CACHE_KEY.getBytes(),
                    cid.getBytes(),
                    field.getBytes(),
                    Boolean.toString(inc).getBytes());
            return null;
        }, true);
    }

    public abstract VodInteractiveActionEnum action();
}
