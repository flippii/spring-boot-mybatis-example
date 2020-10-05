package com.spring.boot.example.mybatis.application;

import com.spring.boot.example.mybatis.application.data.UserData;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserReadService userReadService;

    public Optional<UserData> findById(String id) {
        return Optional.ofNullable(userReadService.findById(id));
    }

    public Optional<UserData> findUserByEmail(String email) {
        return Optional.ofNullable(userReadService.findByEmail(email));
    }

}
