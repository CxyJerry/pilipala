package com.jerry.pilipala.application.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
public class RoleSaveDTO {
    private String id;
    @NotBlank(message = "角色名不得为空")
    private String name;
    @NotNull(message = "角色必须具备权限")
    List<String> permissionIds;
}
