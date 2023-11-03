package com.jerry.pilipala.infrastructure.interceptor;

import com.jerry.pilipala.infrastructure.annotations.FileType;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FileTypeCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod api)) {
            return true;
        }
        if (!api.hasMethodAnnotation(FileType.class)) {
            return true;
        }
        FileType fileType = api.getMethodAnnotation(FileType.class);
        if (Objects.isNull(fileType)) {
            return true;
        }
        List<String> types = Arrays.stream(fileType.types())
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        if (request instanceof MultipartHttpServletRequest multipartHttpServletRequest) {
            Iterator<String> fileNames = multipartHttpServletRequest.getFileNames();
            while (fileNames.hasNext()) {
                String filename = fileNames.next();
                int dotIdx = filename.lastIndexOf(".");
                if (dotIdx == -1) {
                    log.error("file extension miss");
                    throw new BusinessException("文件类型错误", StandardResponse.ERROR);
                }
                String ext = filename.substring(dotIdx + 1).toLowerCase();
                if (!types.contains(ext)) {
                    log.error("require file extension: {} ,but got: {}", types, ext);
                    throw new BusinessException("文件类型错误", StandardResponse.ERROR);
                }
            }
        }
        return true;
    }
}
