package com.jerry.pilipala.domain.interactive.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class UpdateTimeInteractiveParam extends BaseInteractiveParam {
    private String bvId;
    private Long cid;
    private Integer time;
    private String playActionId;
}
