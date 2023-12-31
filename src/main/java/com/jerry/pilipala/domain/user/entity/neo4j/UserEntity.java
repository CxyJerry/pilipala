package com.jerry.pilipala.domain.user.entity.neo4j;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Data
@Node("user")
@Accessors(chain = true)
public class UserEntity {
    @Id
    private String uid = "";
    private String tel = "";
    private String email = "";
    private Long ctime = System.currentTimeMillis();
    @Relationship("Followed")
    private List<UserEntity> followUps = new ArrayList<>();
}
