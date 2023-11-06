package com.jerry.pilipala.domain.user.entity.mongo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("permission")
public class Permission {
    @Id
    private ObjectId id;
    private String name;
    private String value;
    private Long ctime = System.currentTimeMillis();
}
