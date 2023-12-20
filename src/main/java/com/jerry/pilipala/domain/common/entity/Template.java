package com.jerry.pilipala.domain.common.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("template")
public class Template {
    @Id
    private String name;
    private String content;
    private String authorId;
    private Long ctime = System.currentTimeMillis();
}
