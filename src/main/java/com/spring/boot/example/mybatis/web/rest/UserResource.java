package com.spring.boot.example.mybatis.web.rest;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.spring.boot.example.mybatis.application.UserService;
import com.spring.boot.example.mybatis.application.data.UserData;
import com.spring.boot.example.mybatis.application.data.UserWithToken;
import com.spring.boot.example.mybatis.configuration.ApplicationProperties;
import com.spring.boot.example.mybatis.infrastructure.security.TokenProvider;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.web.rest.exception.InvalidRequestException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/users")
public class UserResource {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final ApplicationProperties.Image image;

    public UserResource(PasswordEncoder passwordEncoder,
                        UserRepository userRepository,
                        TokenProvider tokenProvider,
                        UserService userService,
                        ApplicationProperties applicationProperties) {

        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.userService = userService;
        this.image = applicationProperties.getImage();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterParam registerParam, BindingResult bindingResult) {
        validateInput(registerParam, bindingResult);

        User user = new User(
                registerParam.getEmail(),
                registerParam.getUsername(),
                passwordEncoder.encode(registerParam.getPassword()),
                "",
                image.getDefaultImage());

        userRepository.save(user);

        UserData userData = userService.findById(user.getId()).get();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("user", new UserWithToken(userData, tokenProvider.createToken(userData.getUsername()))));
    }

    private void validateInput(RegisterParam registerParam, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }

        if (userRepository.findByUsername(registerParam.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "DUPLICATED", "duplicated username");
        }

        if (userRepository.findByEmail(registerParam.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "DUPLICATED", "duplicated email");
        }

        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }
    }

    @Getter
    @JsonRootName("user")
    @NoArgsConstructor
    static class RegisterParam {

        @NotBlank(message = "can't be empty")
        @Email(message = "should be an email")
        private String email;

        @NotBlank(message = "can't be empty")
        private String username;

        @NotBlank(message = "can't be empty")
        private String password;

    }

}
