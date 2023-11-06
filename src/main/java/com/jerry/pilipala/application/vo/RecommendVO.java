package com.jerry.pilipala.application.vo;

import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RecommendVO {
    private List<PreviewBVodVO> swiper;
    private List<PreviewBVodVO> first;
}
