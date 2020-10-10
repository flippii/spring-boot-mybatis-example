package com.spring.boot.example.mybatis.infrastructure.initializer;

import com.spring.boot.example.mybatis.configuration.ApplicationProperties;
import com.spring.boot.example.mybatis.core.initilizer.DataInitializer;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.UserMapper;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Order(1)
@Component
public class UserDataInitializer implements DataInitializer {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationProperties.Image image;

    public UserDataInitializer(UserMapper userMapper,
                               PasswordEncoder passwordEncoder,
                               ApplicationProperties applicationProperties) {

        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.image = applicationProperties.getImage();
    }

    @Override
    public void initialize() {
        List<User> users = List.of(
                createUser("peter.mayer@example.de", "Peter Meyer", "password1", "m"),
                createUser("paul.panzer@example.com", "Paul Panzer", "password2", "m"),
                createUser("beate.mustermann@example.com", "Beate Mustermann", "password3", "w")
        );

        users.stream()
                .filter(user ->
                        Optional.ofNullable(userMapper.findByEmail(user.getEmail())).isEmpty()
                )
                .forEach(userMapper::insert);
    }

    private User createUser(String email, String username, String password, String bio) {
        return  new User(
                email,
                username,
                passwordEncoder.encode(password),
                bio,
                image.getDefaultImage());
    }

}
