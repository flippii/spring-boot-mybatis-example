package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.data.UserData;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.security.TokenProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
public class TestConfiguration implements WebMvcConfigurer {

    @Autowired
    protected TokenProvider tokenProvider;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userHandlerMethodArgumentResolver());
    }

    @Bean
    public HandlerMethodArgumentResolver userHandlerMethodArgumentResolver() {
        return new UserHandlerMethodArgumentResolver(testUserService());
    }

    @RequiredArgsConstructor
    public static class UserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

        private final TestUserService testUserService;

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(User.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

            return testUserService.getCurrentUser();
        }

    }

    @Bean
    public TestUserService testUserService() {
        return new TestUserService(tokenProvider);
    }

    @RequiredArgsConstructor
    public static class TestUserService {

        private final TokenProvider tokenProvider;

        @Getter
        private User currentUser;

        @Getter
        private UserData userData;

        @PostConstruct
        public void init() {
            String email = "john@jacob.com";
            String username = "johnjacob";
            String defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

            currentUser = new User(
                    email,
                    username,
                    "123",
                    "",
                    defaultAvatar);

            userData = new UserData(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getUsername(),
                    "",
                    currentUser.getImage());
        }

        public String createToken() {
            return createToken(currentUser);
        }

        public String createToken(User user) {
            return tokenProvider.createToken(currentUser.getUsername());
        }

    }

}
