package com.jerry.pilipala.application.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ApplyVO {
    private String id;
    private PreviewUserVO applicant;
    private PreviewUserVO processor;
    private String status;
    private Long ctime;
}
