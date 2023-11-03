package com.jerry.pilipala.infrastructure.config;


import cn.dev33.satoken.interceptor.SaInterceptor;
import com.jerry.pilipala.infrastructure.interceptor.FileTypeCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    private final FileTypeCheckInterceptor fileTypeCheckInterceptor;

    public InterceptorConfig(FileTypeCheckInterceptor fileTypeCheckInterceptor) {
        this.fileTypeCheckInterceptor = fileTypeCheckInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
//        registry.addInterceptor(fileTypeCheckInterceptor).addPathPatterns("/**");
    }
}
