package com.jerry.pilipala.domain.common.services;

public interface SmsService {
    void sendLoginCode(String tel, String code);
}
