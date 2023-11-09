package com.jerry.pilipala.application.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class PermissionVO {
    private String id;
    private String name;
    private String value;
    private Long ctime = System.currentTimeMillis();
}
