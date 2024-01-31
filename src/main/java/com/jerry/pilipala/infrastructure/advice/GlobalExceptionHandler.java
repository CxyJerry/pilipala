package com.jerry.pilipala.infrastructure.advice;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.CommonResponse;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    @ResponseBody
    public Object handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e) {
        log.error("SSE 异步超时");
        e.printStackTrace();
        return new CommonResponse<>(StandardResponse.ERROR.getCode(), "异步请求超时", null);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleBusinessException(BusinessException e) {
        log.info(e.getMessage());
        return new CommonResponse<>(StandardResponse.ERROR.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Object handleNotLoginException(NotLoginException e) {
        log.info(e.getMessage());
        return new CommonResponse<>(StandardResponse.FORBIDDEN.getCode(), "未登录", null);
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleNotPermissionException(NotPermissionException e) {
        log.info(e.getMessage());
        return new CommonResponse<>(StandardResponse.ERROR.getCode(), "权限不足", null);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        assert fieldError != null;
        return new CommonResponse<>(StandardResponse.ERROR.getCode(), fieldError.getDefaultMessage(), null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleConstraintViolationException(ConstraintViolationException e) {
        return new CommonResponse<>(StandardResponse.ERROR.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        return new CommonResponse<>(StandardResponse.ERROR.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleException(Exception e) {
        return new CommonResponse<>(StandardResponse.ERROR.getCode(), e.getMessage(), null);
    }
}
