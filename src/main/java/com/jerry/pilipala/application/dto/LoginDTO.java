package com.jerry.pilipala.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginDTO {
    @NotBlank(message = "手机号不得为空")
    private String tel;
    @NotBlank(message = "验证码不得为空")
    private String verifyCode;
}
