package com.jerry.pilipala.domain.message.service;

import com.jerry.pilipala.application.vo.message.MessageVO;
import com.jerry.pilipala.application.vo.message.TemplateVO;
import com.jerry.pilipala.application.vo.message.UnreadMessageCountVO;
import com.jerry.pilipala.infrastructure.utils.Page;

import java.util.List;

public interface MessageService {
    List<TemplateVO> messageTemplates();

    void saveMessageTemplate(String templateName, String content);

    List<UnreadMessageCountVO> unreadCount(String uid);

    Page<MessageVO> page(String type, int pageNo, int pageSize);

    void deleteMessageTemplate(String name);
}
