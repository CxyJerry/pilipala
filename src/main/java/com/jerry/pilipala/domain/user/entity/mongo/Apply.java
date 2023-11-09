package com.jerry.pilipala.domain.user.entity.mongo;

import com.jerry.pilipala.infrastructure.enums.ApplyStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("apply")
public class Apply {
    @Id
    private ObjectId id;
    private String applicantId = "";
    private String processorId = "";
    private String status = ApplyStatusEnum.WAITING.getStatus();
    private Long ctime = System.currentTimeMillis();
    private Long mtime = System.currentTimeMillis();
}
