package com.jerry.pilipala.application.vo.user;

import com.jerry.pilipala.application.vo.vod.VodVO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DynamicVO {
    private UserVO userVO;
    private VodVO vodVO;
    private Long ctime;
}
