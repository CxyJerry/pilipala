package com.jerry.pilipala.application.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PreviewUserVO {
    private String uid = "";
    private String nickName = "";
    private String avatar = "";
    private String intro = "";
}
