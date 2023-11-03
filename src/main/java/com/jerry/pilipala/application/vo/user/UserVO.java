package com.jerry.pilipala.application.vo.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class UserVO extends PreviewUserVO {
    private Long fansCount = 0L;
    private Long followCount = 0L;
}
