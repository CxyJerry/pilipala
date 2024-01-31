package com.jerry.pilipala.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VideoPostDTO {
    @NotBlank(message = "BVID不得为空")
    private String bvId;
    @NotNull(message = "稿件ID不得为空")
    private Long cid;
    @NotBlank(message = "视频封面不得为空")
    private String coverUrl;
    @NotBlank(message = "视频标题不得为空")
    private String title;
    @NotBlank(message = "生产类型不得为空")
    private String gcType;
    @NotBlank(message = "分区不得为空")
    private String partition;
    @NotBlank(message = "子分区不得为空")
    private String subPartition;
    @NotNull(message = "标签不得为空")
    private List<String> labels;
    private String desc;
}
