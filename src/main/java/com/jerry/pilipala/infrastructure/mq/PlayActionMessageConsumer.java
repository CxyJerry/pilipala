package com.jerry.pilipala.infrastructure.mq;

import com.jerry.pilipala.infrastructure.annotations.RedisStreamListener;
import com.jerry.pilipala.infrastructure.enums.redis.VodCacheKeyEnum;
import com.jerry.pilipala.infrastructure.utils.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

@Slf4j
@RedisStreamListener(
        topic = VodCacheKeyEnum.StreamKey.PLAY_ACTION,
        batchSize = 50,
        pollTimeout = 5
)
public class PlayActionMessageConsumer implements StreamListener<String, MapRecord<String, String, String>> {
    private final MongoTemplate mongoTemplate;
    private final JsonHelper jsonHelper;

    public PlayActionMessageConsumer(MongoTemplate mongoTemplate, JsonHelper jsonHelper) {
        this.mongoTemplate = mongoTemplate;
        this.jsonHelper = jsonHelper;
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
//        Map<String, String> map = message.getValue();
//        VodInteractiveAction action = null;
//        try {
//            action = jsonHelper.parse(map.get("action"), VodInteractiveAction.class);
//            log.info("receive message: {}", action);
//            Long cid = Long.parseLong(action.getParams().get("cid").toString());
//
//            // 写入 redis 缓存
//        } catch (BusinessException e) {
//            log.error("play action message handle error", e);
//        }
    }
}
