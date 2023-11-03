package com.jerry.pilipala.application.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserInfoBO {
    private String uid;
    private List<String> permissions;
}
