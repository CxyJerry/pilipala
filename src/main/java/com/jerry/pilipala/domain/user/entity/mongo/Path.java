package com.jerry.pilipala.domain.user.entity.mongo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("path")
public class Path {
    @Id
    private ObjectId id;
    private String path;
    private String permissionId;
    private Boolean deleted = false;
    private Long ctime = System.currentTimeMillis();
}
