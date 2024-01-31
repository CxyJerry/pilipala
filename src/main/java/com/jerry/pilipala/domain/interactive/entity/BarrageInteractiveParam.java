package com.jerry.pilipala.domain.interactive.entity;

import com.jerry.pilipala.application.dto.DanmakuDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BarrageInteractiveParam extends BaseInteractiveParam {
    private Long cid;
    private DanmakuDTO danmaku;
}
