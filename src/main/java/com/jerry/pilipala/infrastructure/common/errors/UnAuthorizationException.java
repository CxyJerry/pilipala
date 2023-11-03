package com.jerry.pilipala.infrastructure.common.errors;

import com.jerry.pilipala.infrastructure.common.interfaces.IResponse;

public class UnAuthorizationException extends BusinessException{
    public UnAuthorizationException(IResponse response) {
        super(response);
    }

    public UnAuthorizationException(String message, IResponse response) {
        super(message, response);
    }

    public UnAuthorizationException(String message, IResponse response, Object[] args) {
        super(message, response, args);
    }
}
