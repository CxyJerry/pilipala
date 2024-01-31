package com.jerry.pilipala.domain.vod.entity.mongo.interactive;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("vod_interactive_action")
public class VodInteractiveAction {
    @Id
    private String id;
    private String uid;
    private String interactiveAction;
    private Object param;
    private Long ctime;
    private Long mtime;
}
