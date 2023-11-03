package com.jerry.pilipala.domain.common.services;

import com.jerry.pilipala.infrastructure.utils.Page;
import com.jerry.pilipala.application.vo.SearchResultVO;

public interface SearchService {
    Page<SearchResultVO> search(String type, String search, Integer pageNo, Integer pageSize);

}
