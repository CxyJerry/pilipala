package com.jerry.pilipala.application.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PathVO {
    private String id;
    private String path;
    private String permissionName = "";
    private Long ctime;
}
