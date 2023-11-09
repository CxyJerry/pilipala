package com.jerry.pilipala.application.vo.bvod;

import com.jerry.pilipala.application.vo.user.PreviewUserVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BVodVO extends BaseBVodVO {
    private PreviewUserVO author;

}
