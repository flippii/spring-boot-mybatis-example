package com.spring.boot.example.mybatis.web.rest.exception;

import lombok.Getter;
import org.springframework.validation.Errors;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final Errors errors;

    public InvalidRequestException(Errors errors) {
        super("");
        this.errors = errors;
    }

}
