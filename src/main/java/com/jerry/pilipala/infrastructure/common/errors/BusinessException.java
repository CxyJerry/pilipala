package com.jerry.pilipala.infrastructure.common.errors;

import com.jerry.pilipala.infrastructure.common.interfaces.IResponse;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.text.MessageFormat;


@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException implements IResponse {
    private final int code;
    private final String message;
    private final IResponse response;

    public BusinessException(IResponse response) {
        this("", response, null);
    }

    public BusinessException(String message, IResponse response) {
        this(message, response, null);
    }

    public BusinessException(String message, IResponse response, Object[] args) {
        super(message == null ? response.getMessage() : message);
        this.code = response.getCode();
        this.response = response;
        this.message = MessageFormat.format(super.getMessage(), args);
    }


    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public static BusinessException businessError(String message) {
        return new BusinessException(message, StandardResponse.ERROR);
    }
}
