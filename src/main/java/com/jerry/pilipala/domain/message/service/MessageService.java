package com.jerry.pilipala.domain.message.service;

import com.jerry.pilipala.application.vo.message.MessageVO;
import com.jerry.pilipala.infrastructure.utils.Page;

public interface MessageService {
    void send(String senderId, String receiverId, String msg);

    long unreadCount(String uid);

    Page<MessageVO> page(int pageNo, int pageSize);

}
