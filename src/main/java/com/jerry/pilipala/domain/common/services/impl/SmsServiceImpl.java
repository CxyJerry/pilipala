package com.jerry.pilipala.domain.common.services.impl;

import com.jerry.pilipala.domain.common.services.SmsService;
import com.jerry.pilipala.infrastructure.config.SmsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {
    private final RestTemplate restTemplate;
    private final SmsConfig smsConfig;

    public SmsServiceImpl(RestTemplate restTemplate,
                          SmsConfig smsConfig) {
        this.restTemplate = restTemplate;
        this.smsConfig = smsConfig;
    }


    @Override
    public void sendLoginCode(String tel, String code) {
//        Map<String, Object> params = new HashMap<>() {
//            {
//                put("action", "sms.message.send");
//                put("accessKeyId", smsConfig.getAccessKey());
//            }
//        };
//        Object response = restTemplate.postForObject("https://uni.apistd.com",,Object.class, params);
//        log.info("{}", response);
    }
}
