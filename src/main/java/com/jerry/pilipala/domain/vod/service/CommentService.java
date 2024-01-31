package com.jerry.pilipala.domain.vod.service;

import com.jerry.pilipala.application.dto.CommentDTO;
import com.jerry.pilipala.application.vo.vod.CommentVO;
import com.jerry.pilipala.infrastructure.utils.Page;

public interface CommentService {
    void post(String uid, CommentDTO commentDTO);

    Page<CommentVO> get(String cid, String parentCommentId, Integer pageNo, Integer pageSize);

    void delete(String uid, Long cid, String commentId);
}
