package com.jerry.pilipala.infrastructure.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jerry.pilipala.infrastructure.annotations.IgnoreLog;
import com.jerry.pilipala.infrastructure.utils.JsonHelper;
import com.jerry.pilipala.infrastructure.utils.RequestUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Aspect
@Slf4j
@Component
public class ApiLoggerAspect {
    private final HttpServletRequest request;
    private final JsonHelper jsonHelper;

    public ApiLoggerAspect(HttpServletRequest request,
                           JsonHelper jsonHelper) {
        this.request = request;
        this.jsonHelper = jsonHelper;
    }

    @Pointcut("execution(* com.jerry.pilipala.interfaces..*Controller.*(..))")
    public void apiLog() {
    }

    @Around("apiLog()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        LogModel logModel = new LogModel();
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        // 解析请求参数
        parseRequest(point, logModel);
        // 设置返回值
        logModel.setResponse(result);
        long end = System.currentTimeMillis();
        logModel.setTimestamp(end)
                .setCost(end - start);

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        if (!method.isAnnotationPresent(IgnoreLog.class)) {
            log.info(jsonHelper.as(logModel));
        }

        return result;
    }

    @AfterThrowing(value = "apiLog()", throwing = "e")
    public void afterThrowing(JoinPoint point, Throwable e) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        StackTraceElement[] stackTrace = e.getStackTrace();
        String stackTracing = Arrays.toString(stackTrace).replace("[", "").replace("]", "");
        StackTraceElement stackTraceElement = e.getStackTrace()[0];
        ExceptionInfo exceptionInfo = new ExceptionInfo();
        exceptionInfo.setMessage(e.getMessage())
                .setFilename(stackTraceElement.getFileName())
                .setClassName(stackTraceElement.getClassName())
                .setMethodName(stackTraceElement.getMethodName())
                .setLineNumber(stackTraceElement.getLineNumber())
                .setDetails(stackTracing);
        LogModel logModel = new LogModel();
        logModel.setException(exceptionInfo);
        // 解析请求参数
        parseRequest((ProceedingJoinPoint) point, logModel);
        long end = System.currentTimeMillis();
        logModel.setTimestamp(end)
                .setCost(end - start);
        log.info(jsonHelper.as(logModel));
    }

    private void parseRequest(ProceedingJoinPoint point, LogModel logModel) {
        String ip = RequestUtil.getIpAddress(request);
        String method = request.getMethod();
        StringBuffer path = request.getRequestURL();

        Object[] args = point.getArgs();
        CodeSignature signature = (CodeSignature) point.getSignature();
        String[] paramNames = signature.getParameterNames();
        Map<String, Object> params = new HashMap<>(paramNames.length);
        for (int i = 0; i < paramNames.length; i++) {
            params.put(paramNames[i], Objects.toString(args[i]));
        }
        logModel.setIp(ip)
                .setMethod(method)
                .setPath(path.toString())
                .setParams(params);
    }

    @Data
    @Accessors(chain = true)
    public static class LogModel {
        private String ip;
        private String method;
        private String path;
        private Map<String, Object> params;
        private Object response;
        private ExceptionInfo exception;
        private Long timestamp;
        private Long cost;
    }

    @Data
    @Accessors(chain = true)
    public static class ExceptionInfo {
        private String message;
        private String filename;
        private String className;
        private String methodName;
        private Integer lineNumber;
        private Object details;
    }
}
