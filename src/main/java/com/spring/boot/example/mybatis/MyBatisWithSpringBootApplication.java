package com.spring.boot.example.mybatis;

import com.spring.boot.example.mybatis.configuration.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class MyBatisWithSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBatisWithSpringBootApplication.class, args);
    }

}
