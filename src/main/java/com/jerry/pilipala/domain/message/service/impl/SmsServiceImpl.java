package com.jerry.pilipala.domain.message.service.impl;

import com.jerry.pilipala.domain.message.entity.Sms;
import com.jerry.pilipala.domain.message.service.SmsService;
import com.jerry.pilipala.infrastructure.config.SmsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {
    private final RestTemplate restTemplate;
    private final SmsConfig smsConfig;

    public SmsServiceImpl(RestTemplate restTemplate, SmsConfig smsConfig) {
        this.restTemplate = restTemplate;
        this.smsConfig = smsConfig;
    }

    @Override
    public void sendCode(String tel, String code, Integer expireMinutes) {
        HashMap<String, Object> templateData = new HashMap<>();
        templateData.put("code", code);
        templateData.put("ttl", expireMinutes);
        Sms sms = new Sms().setTo(tel)
                .setSignature("PiliPala")
                .setTemplateId("pub_verif_login_ttl")
                .setTemplateData(templateData);
        Map map = restTemplate.postForObject("https://uni.apistd.com/?action=sms.message.send&accessKeyId=%s".formatted(smsConfig.getAccessKey()),
                sms, Map.class);
        if (Objects.isNull(map)) {
            log.error("短信发送失败");
        } else {
            log.info("send sms ,response: {}", map);
        }
    }
}
