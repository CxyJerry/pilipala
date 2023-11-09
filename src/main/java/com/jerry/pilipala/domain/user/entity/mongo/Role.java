package com.jerry.pilipala.domain.user.entity.mongo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@Document("role")
public class Role {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private String name = "";
    private List<String> permissionIds = new ArrayList<>();
    private Boolean deleted = false;
    private Long ctime = System.currentTimeMillis();
}
