package com.jerry.pilipala.application.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TemplateDTO {
    private String name;
    private String content;
}
