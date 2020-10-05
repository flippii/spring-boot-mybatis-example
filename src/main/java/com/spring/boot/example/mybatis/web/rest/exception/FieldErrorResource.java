package com.spring.boot.example.mybatis.web.rest.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldErrorResource {

    String resource;
    String field;
    String code;
    String message;

}
