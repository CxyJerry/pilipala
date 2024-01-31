package com.jerry.pilipala.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;



@Data
@Accessors(chain = true)
public class DanmakuDTO {
    @NotNull(message = "弹幕颜色不得为空")
    private Integer color;
    // 稿件ID
    @NotNull(message = "稿件ID不得为空")
    private Long id;
    @NotBlank(message = "弹幕内容不得为空")
    private String text;
    @NotNull(message = "弹幕时间不得为空")
    private Double time;
    @NotNull(message = "弹幕位置不得为空")
    private Integer type;
}
