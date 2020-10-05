package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.data.UserData;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.UserReadService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

abstract class TestWithCurrentUser {

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected UserReadService userReadService;

    @Autowired
    protected TestConfiguration.TestUserService testUserService;

    protected User currentUser;
    protected UserData userData;
    protected String token;

    @BeforeEach
    void before() {
        userFixture();
        tokenFixture();
    }

    protected void userFixture() {
        currentUser = testUserService.getCurrentUser();

        when(userRepository.findByUsername(eq(currentUser.getUsername()))).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail(eq(currentUser.getEmail()))).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(eq(currentUser.getId()))).thenReturn(Optional.of(currentUser));

        userData = testUserService.getUserData();

        when(userReadService.findById(eq(currentUser.getId()))).thenReturn(userData);
    }

    protected void tokenFixture() {
        token = testUserService.createToken();
    }

}
