package com.spring.boot.example.mybatis.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Bean
    public Docket apiFirstDocket(ApplicationProperties applicationProperties) {
        ApplicationProperties.ApiDocs properties = applicationProperties.getApiDocs();

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("openapi")
                .host(properties.getHost())
                .protocols(Set.of(properties.getProtocols()))
                .apiInfo(createApiInfo(properties))
                .useDefaultResponseMessages(properties.isUseDefaultResponseMessages())
                .forCodeGeneration(true)
                .directModelSubstitute(ByteBuffer.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .ignoredParameterTypes(Pageable.class)
                .securityContexts(List.of(securityContext(properties)))
                .securitySchemes(List.of(apiKey()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.spring.boot.example.mybatis.web"))
                //.paths(regex(properties.getDefaultIncludePattern()))
                .build();
    }

    private ApiInfo createApiInfo(ApplicationProperties.ApiDocs properties) {
        return new ApiInfo(
                "API First " + properties.getTitle(),
                properties.getDescription(),
                properties.getVersion(),
                properties.getTermsOfServiceUrl(),
                createContact(properties),
                properties.getLicense(),
                properties.getLicenseUrl(),
                new ArrayList<>()
        );
    }

    private Contact createContact(ApplicationProperties.ApiDocs properties) {
        return new Contact(
                properties.getContactName(),
                properties.getContactUrl(),
                properties.getContactEmail()
        );
    }

    private ApiKey apiKey() {
        return new ApiKey("JWT", AUTHORIZATION_HEADER, "header");
    }

    private SecurityContext securityContext(ApplicationProperties.ApiDocs properties) {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(regex(properties.getDefaultIncludePattern()))
                .build();
    }

    List<SecurityReference> defaultAuth() {
        return List.of(new SecurityReference(
                "JWT",
                List.of(new AuthorizationScope("global", "accessEverything")).toArray(AuthorizationScope[]::new)
        ));
    }

}
