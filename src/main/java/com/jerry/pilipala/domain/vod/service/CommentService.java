package com.jerry.pilipala.domain.vod.service;

import com.jerry.pilipala.application.dto.CommentDTO;
import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.application.vo.CommentVO;

public interface CommentService {
    CommentVO post(CommentDTO commentDTO);

    Page<CommentVO> get(String cid, String parentCommentId, Integer pageNo, Integer pageSize);
}
