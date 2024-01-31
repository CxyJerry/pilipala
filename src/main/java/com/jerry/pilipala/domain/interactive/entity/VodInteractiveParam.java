package com.jerry.pilipala.domain.interactive.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class VodInteractiveParam extends BaseInteractiveParam {
    private Long cid;
}
