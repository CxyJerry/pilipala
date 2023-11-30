package com.jerry.pilipala.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("qiniu")
public class Qiniu {
    private String accessKey;
    private String secretKey;
    private String imgBucket;
    private String vodBucket;
    private String imgDomain;
    private String vodDomain;
}
