package com.jerry.pilipala.domain.user.entity.mongo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("fans")
@Accessors(chain = true)
public class Fans {
    @Id
    private ObjectId id;
    @Indexed
    private String fansId;
    @Indexed
    private String upId;
    private Byte deleted = 0x01;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
