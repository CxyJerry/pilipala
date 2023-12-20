package com.jerry.pilipala.domain.user.entity.mongo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("user")
@Accessors(chain = true)
public class User {
    @Id
    private ObjectId uid;
    private String tel;
    private String email;
    @TextIndexed
    private String nickname = "unknown";
    private String intro = "";
    private String avatar = "/file/cover/nav_icon_avatar_nor.svg";
    private String roleId = "";
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();

    public static User UNKNOWN = new User()
            .setUid(new ObjectId())
            .setTel("")
            .setEmail("")
            .setNickname("unknown")
            .setAvatar("")
            .setRoleId("");
}
