package com.jerry.pilipala.domain.vod.entity.mongo.interactive;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Accessors(chain = true)
@Document("vod_interactive_action")
public class VodInteractiveAction {
    @Id
    private String id;
    private String uid;
    private String interactiveAction;
    private Map<String, Object> params;
    private Long ctime;
    private Long mtime;
}
