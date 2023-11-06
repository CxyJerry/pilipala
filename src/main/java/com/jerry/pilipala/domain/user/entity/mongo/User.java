package com.jerry.pilipala.domain.user.entity.mongo;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document("user")
@Accessors(chain = true)
public class User {
    @Id
    private ObjectId uid;
    private String tel;
    @TextIndexed
    private String nickname = "unknown";
    private String intro = "";
    private String avatar = "";
    private List<String> permission = new ArrayList<>();
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();

    public static User UNKNOWN = new User()
            .setUid(new ObjectId())
            .setTel("123456")
            .setNickname("unknown")
            .setAvatar("")
            .setPermission(Lists.newArrayList());
}
