package com.jerry.pilipala.application.vo;

import com.jerry.pilipala.application.vo.bvod.PreviewBVodVO;
import com.jerry.pilipala.infrastructure.utils.Pair;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RecommendVO {
    private List<PreviewBVodVO> swiper;
    private List<PreviewBVodVO> first;
    private List<Pair<String, List<PreviewBVodVO>>> types;
}
