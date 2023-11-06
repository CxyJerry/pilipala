package com.jerry.pilipala.domain.common.services.impl;

import com.jerry.pilipala.application.vo.SearchResultVO;
import com.jerry.pilipala.domain.common.services.SearchService;
import com.jerry.pilipala.domain.user.entity.mongo.User;
import com.jerry.pilipala.domain.user.service.UserService;
import com.jerry.pilipala.domain.vod.entity.mongo.vod.VodInfo;
import com.jerry.pilipala.domain.vod.service.VodService;
import com.jerry.pilipala.infrastructure.enums.SearchTypeEnum;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private final UserService userService;

    private final VodService vodService;

    public SearchServiceImpl(MongoTemplate mongoTemplate,
                             RedisTemplate<String, Object> redisTemplate,
                             UserService userService,
                             VodService vodService) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.vodService = vodService;
    }

    @Override
    public Page<SearchResultVO> search(String type, String search, Integer pageNo, Integer pageSize) {
        type = SearchTypeEnum.parse(type);
        return switch (type) {
            case "video" -> searchVideo(search, pageNo, pageSize);
            case "user" -> searchUser(search, pageNo, pageSize);
            default -> new Page<SearchResultVO>()
                    .setPageNo(pageNo)
                    .setPageSize(pageSize)
                    .setTotal(0L)
                    .setPage(new ArrayList<>());
        };
    }

    private Page<SearchResultVO> searchVideo(String search, Integer pageNo, Integer pageSize) {
        Criteria criteria = Criteria.where("title")
                .regex(".*%s.*".formatted(search), "i");
        long count = mongoTemplate.count(new Query(criteria), VodInfo.class);
        List<VodInfo> vodInfoList = mongoTemplate.find(
                new Query(criteria)
                        .skip((long) Math.max(pageNo - 1, 0) * pageSize)
                        .limit(pageSize),
                VodInfo.class);
        List previewBVodVOS = vodService.buildPreviewBVodList(vodInfoList);
        Page<SearchResultVO> page = new Page<>();
        page.setPageNo(pageNo)
                .setPageSize(pageSize)
                .setTotal(count)
                .setPage(Collections.singletonList(
                        new SearchResultVO()
                                .setType(SearchTypeEnum.TITLE.getField())
                                .setResult(previewBVodVOS))
                );
        return page;
    }

    private Page<SearchResultVO> searchUser(String search, Integer pageNo, Integer pageSize) {
        Criteria criteria = Criteria.where("nickname").regex(".*%s.*".formatted(search), "i");
        long count = mongoTemplate.count(new Query(criteria), User.class);
        List<User> userList = mongoTemplate.find(
                new Query(criteria)
                        .skip((long) Math.max(pageNo - 1, 0) * pageSize)
                        .limit(pageSize),
                User.class);

        Set<String> uidSet = userList
                .stream()
                .map(user -> user.getUid().toString()).collect(Collectors.toSet());

        List userVOList = userService.userVoList(uidSet);

        Page<SearchResultVO> page = new Page<>();
        page.setPageNo(pageNo)
                .setPageSize(pageSize)
                .setTotal(count)
                .setPage(Collections.singletonList(
                        new SearchResultVO()
                                .setType(SearchTypeEnum.USER.getField())
                                .setResult(userVOList))
                );
        return page;
    }
}
