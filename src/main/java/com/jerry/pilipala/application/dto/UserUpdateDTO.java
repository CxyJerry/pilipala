package com.jerry.pilipala.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @NotBlank(message = "昵称不得为空")
    private String nickName;
    private String intro;
    private String avatar;
    private String announcement;
}
