package com.jerry.pilipala.domain.user.entity.mongo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("user_dynamic")
public class Dynamic {
    @Id
    private String uid;
    private Long cid;
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
