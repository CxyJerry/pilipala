package com.jerry.pilipala.domain.interactive.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PlayInteractiveParam extends BaseInteractiveParam {
    private Long cid;
    private String authorUid;
    private Boolean valid;
}
