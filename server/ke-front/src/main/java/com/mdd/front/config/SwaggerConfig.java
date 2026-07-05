package com.mdd.front.config;

import com.mdd.common.config.GlobalConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Value("${like.swagger.enabled}")
    private boolean enabled;

    @Value("${like.swagger.pathMapping}")
    private String pathMapping;

    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .enable(enabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.mdd.front"))
                .build()
                .pathMapping(pathMapping)
                .securitySchemes(Collections.singletonList(apiKey()))
                .securityContexts(Collections.singletonList(securityContext()));
    }

    private ApiInfo apiInfo(){
        String author = "";
        String url = "";
        String email = "";

        return new ApiInfoBuilder()
                .title("KeAdmin【前台】接口文档")
                .description("KeAdmin【前台】接口文档")
                .version(GlobalConfig.version)
                .contact(new Contact(author, url, email))
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey("token", "token", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .operationSelector(o -> true) // 应用到所有接口
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope scope = new AuthorizationScope("global", "accessEverything");
        return Collections.singletonList(new SecurityReference("token", new AuthorizationScope[]{scope}));
    }
}
