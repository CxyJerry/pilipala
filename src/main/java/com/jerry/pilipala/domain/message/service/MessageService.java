package com.jerry.pilipala.domain.message.service;

public interface MessageService {
    void send(String senderId, String receiverId, String msg);

    long unreadCount(String uid);
}
