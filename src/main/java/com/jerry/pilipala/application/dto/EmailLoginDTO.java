package com.jerry.pilipala.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class EmailLoginDTO {
    @NotBlank(message = "邮箱不得为空")
    private String email;
    @NotBlank(message = "验证码不得为空")
    private String verifyCode;
}
