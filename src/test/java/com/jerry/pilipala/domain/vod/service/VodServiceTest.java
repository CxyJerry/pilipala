package com.jerry.pilipala.domain.vod.service;

import com.jerry.pilipala.application.vo.vod.VodVO;
import com.jerry.pilipala.domain.vod.service.impl.VodServiceImpl;
import com.jerry.pilipala.infrastructure.utils.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class VodServiceTest {
    @Autowired
    private VodServiceImpl vodService;

    @Test
    void contextLoads() {
        Page<VodVO> passed = vodService.reviewPage(1, 10, "passed");
        System.out.println(passed);
        assert !passed.getTotal().equals(0L);
    }
}
