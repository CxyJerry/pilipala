package com.jerry.pilipala.interfaces.web;

import com.jerry.pilipala.application.vo.SearchResultVO;
import com.jerry.pilipala.domain.common.services.SearchService;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }


    @GetMapping("/get")
    public CommonResponse<?> search(
            @RequestParam("type") String type,
            @RequestParam("search") @NotBlank(message = "关键词不得为空") String search,
            @RequestParam("page_no") @Min(value = 1, message = "非法页码") @Max(value = 1000, message = "非法页码") Integer pageNo,
            @RequestParam("page_size") @Min(value = 1, message = "非法数据量") @Max(value = 100, message = "非法数据量") Integer pageSize) {
        Page<SearchResultVO> page = searchService.search(type, search, pageNo, pageSize);
        return CommonResponse.success(page);
    }
}
