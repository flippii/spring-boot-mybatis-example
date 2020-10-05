package com.spring.boot.example.mybatis.web.rest.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {

        ErrorResource error = createErrorResource(ex.getBindingResult().getFieldErrors());

        headers.setContentType(MediaType.APPLICATION_JSON);

        return handleExceptionInternal(ex, error, headers, UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler({InvalidRequestException.class})
    public ResponseEntity<?> handleInvalidRequest(RuntimeException e, WebRequest request) {
        InvalidRequestException ex = (InvalidRequestException) e;

        ErrorResource error = createErrorResource(ex.getErrors().getFieldErrors());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return handleExceptionInternal(e, error, headers, UNPROCESSABLE_ENTITY, request);
    }

    private ErrorResource createErrorResource(List<FieldError> fieldErrors) {
        List<FieldErrorResource> errorResources = fieldErrors.stream()
                .map(fieldError ->
                        new FieldErrorResource(
                                fieldError.getObjectName(),
                                fieldError.getField(),
                                fieldError.getCode(),
                                fieldError.getDefaultMessage())
                )
                .collect(Collectors.toList());

        return new ErrorResource(errorResources);
    }

}
