package com.jerry.pilipala.domain.message.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class Sms {
    private String to;
    private String signature;
    private String content;
    private String templateId;
    private Map<String, Object> templateData;
}
