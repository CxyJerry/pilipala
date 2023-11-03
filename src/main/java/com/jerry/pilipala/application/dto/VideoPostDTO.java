package com.jerry.pilipala.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class VideoPostDTO {
    private String bvId;
    private Long cid;
    private String coverUrl;
    private String title;
    private String gcType;
    private String partition;
    private String subPartition;
    private List<String> labels;
    private String desc;
}
