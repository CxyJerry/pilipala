package com.jerry.pilipala.infrastructure.config;

import com.jerry.pilipala.infrastructure.annotations.RedisStreamListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Configuration
public class RedisStreamConfig {
    private final ApplicationContext applicationContext;
    private final RedisConnectionFactory redisConnectionFactory;
    private final Environment environment;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisStreamConfig(ApplicationContext applicationContext,
                             RedisConnectionFactory redisConnectionFactory,
                             Environment environment,
                             RedisTemplate<String, Object> redisTemplate) {
        this.applicationContext = applicationContext;
        this.redisConnectionFactory = redisConnectionFactory;
        this.environment = environment;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void registerStreamMessageListenerList() {
        Map<String, Object> listeners = applicationContext.getBeansWithAnnotation(RedisStreamListener.class);
        listeners.values().forEach(bean -> {
            if (!(bean instanceof StreamListener)) {
                log.error("redis stream message listener failed to registered");
            }

            RedisStreamListener redisStreamListener = bean.getClass().getAnnotation(RedisStreamListener.class);
            register(redisStreamListener, (StreamListener) bean);
        });
    }


    public void register(RedisStreamListener redisStreamListener, StreamListener listener) {
        StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .batchSize(redisStreamListener.batchSize())
                        .pollTimeout(Duration.ofSeconds(redisStreamListener.pollTimeout()))
                        .errorHandler(e -> {
                            log.error("redis stream listener read error,", e);
                        })
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer =
                StreamMessageListenerContainer.create(redisConnectionFactory, streamMessageListenerContainerOptions);

        streamMessageListenerContainer
                .receive(StreamOffset.fromStart(redisStreamListener.topic()), listener);

        streamMessageListenerContainer.start();
        log.info("redis stream message listener: [{}] registered.", redisStreamListener.topic());
    }

}
