package com.jerry.pilipala.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
public class Knife4JConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("测试API")
                        .version("1.0")
                        .description("项目学习")
                        .termsOfService("http://localhost:8080/swaggeer-ui.html")
                );
    }
//    @Bean(value = "defaultApi")
//    public Docket defaultApi() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(new ApiInfoBuilder()
//                        .title("api title")
//                        .description("")
//                        .termsOfServiceUrl("http://localhost:8080/swaggeer-ui.html")
//                        .version("1.0")
//                        .build())
//                .groupName("1.0版本")
//                .select()
//                .apis(RequestHandlerSelectors.basePackage(basePackage))
//                .paths(PathSelectors.any())
//                .build();
//     }
}
