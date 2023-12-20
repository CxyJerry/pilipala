package com.jerry.pilipala.application.vo.message;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UnreadMessageCountVO {
    private String type;
    private Integer count;
}
