package com.jerry.pilipala.application.vo.bvod;

import com.jerry.pilipala.application.vo.vod.VodVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class BaseBVodVO {
    private String bvId;
    private List<VodVO> vodList;
    private Long mtime;
}
