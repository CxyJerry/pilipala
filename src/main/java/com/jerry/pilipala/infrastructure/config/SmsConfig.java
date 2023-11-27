package com.jerry.pilipala.infrastructure.config;

import com.apistd.uni.Uni;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Data
@Slf4j
@Configuration
@ConfigurationProperties("sms")
public class SmsConfig {
    private String accessKey;
    private String accessSecret;
    private String signature;

    @PostConstruct
    public void initSms() {
        Uni.init(accessKey, accessSecret);
        log.info("sms 初始化完成");
    }
}
