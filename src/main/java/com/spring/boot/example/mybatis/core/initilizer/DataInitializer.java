package com.spring.boot.example.mybatis.core.initilizer;

import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public interface DataInitializer {

    @Transactional
    void initialize();

}
