package com.jerry.pilipala.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class EmailLoginDTO {
    @NotBlank(message = "邮箱不得为空")
    private String email;
    @NotBlank(message = "验证码不得为空")
    private String verifyCode;
}
