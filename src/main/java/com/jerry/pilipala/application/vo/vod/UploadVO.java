package com.jerry.pilipala.application.vo.vod;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UploadVO {
    private String token;
    private String filename;
}
