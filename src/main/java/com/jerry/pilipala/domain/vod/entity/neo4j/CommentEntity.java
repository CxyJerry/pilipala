package com.jerry.pilipala.domain.vod.entity.neo4j;

import com.jerry.pilipala.domain.user.entity.neo4j.UserEntity;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Data
@Node("vod-comment")
@Accessors(chain = true)
public class CommentEntity {
    @Id
    private String id;
    @Relationship("ReplyFor")
    private CommentEntity parentComment;
    @Relationship(type="SendBy")
    private UserEntity author;
    @Relationship("BelongTo")
    private VodInfoEntity vod;
    private String content;
    private Long ctime = System.currentTimeMillis();
}
