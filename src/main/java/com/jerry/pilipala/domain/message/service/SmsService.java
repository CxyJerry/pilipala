package com.jerry.pilipala.domain.message.service;

public interface SmsService {
    void sendCode(String tel, String code,Integer expireMinutes);
}
