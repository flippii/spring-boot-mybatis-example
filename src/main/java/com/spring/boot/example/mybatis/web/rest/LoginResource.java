package com.spring.boot.example.mybatis.web.rest;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.spring.boot.example.mybatis.application.AuthService;
import com.spring.boot.example.mybatis.application.UserService;
import com.spring.boot.example.mybatis.application.data.UserData;
import com.spring.boot.example.mybatis.application.data.UserWithToken;
import com.spring.boot.example.mybatis.infrastructure.security.TokenProvider;
import com.spring.boot.example.mybatis.web.rest.exception.InvalidRequestException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/users")
public class LoginResource {

    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginParam loginParam, BindingResult bindingResult) {
        Authentication authentication = authService.login(loginParam.getEmail(), loginParam.getPassword());
        String token = tokenProvider.createToken(authentication.getName());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        Optional<UserData> userData = userService.findUserByEmail(loginParam.getEmail());

        if (userData.isPresent()) {
            return new ResponseEntity<>(Map.of("user", new UserWithToken(userData.get(), token)), httpHeaders, HttpStatus.OK);
        } else {
            bindingResult.rejectValue("email", "UNKNOWN", "email not found");
            throw new InvalidRequestException(bindingResult);
        }
    }

    @Getter
    @JsonRootName("user")
    @NoArgsConstructor
    static class LoginParam {

        @NotBlank(message = "can't be empty")
        @Email(message = "should be an email")
        private String email;

        @NotBlank(message = "can't be empty")
        private String password;

    }

}
