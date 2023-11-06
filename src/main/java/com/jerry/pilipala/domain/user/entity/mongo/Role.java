package com.jerry.pilipala.domain.user.entity.mongo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Accessors(chain = true)
@Document("role")
public class Role {
    @Id
    private ObjectId id;
    private String name;
    private List<String> permissionIds;
    private Long ctime = System.currentTimeMillis();
}
