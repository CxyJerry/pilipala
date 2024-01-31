package com.jerry.pilipala.domain.interactive.entity;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseInteractiveParam {
    protected String selfUid = "unknown";
}
