package com.jerry.pilipala.application.vo.bvod;

import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BVodVO extends BaseBVodVO {
    private PreviewUserVO author;

}
