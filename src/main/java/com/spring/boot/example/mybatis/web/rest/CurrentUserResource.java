package com.spring.boot.example.mybatis.web.rest;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.spring.boot.example.mybatis.application.UserService;
import com.spring.boot.example.mybatis.application.data.UserData;
import com.spring.boot.example.mybatis.application.data.UserWithToken;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.web.rest.exception.InvalidRequestException;
import com.spring.boot.example.mybatis.web.rest.exception.ResourceNotFoundException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/user")
public class CurrentUserResource {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> currentUser(@AuthenticationPrincipal User currentUser,
                                         @RequestHeader(value = "Authorization") String authorization) {

        return userService.findById(currentUser.getId())
                .map(userData -> createResponse(userData, authorization))
                .orElseThrow(ResourceNotFoundException::new);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User currentUser,
                                           @RequestHeader(value = "Authorization") String authorization,
                                           @Valid @RequestBody UpdateUserParam updateUserParam,
                                           BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }

        checkUniquenessOfUsernameAndEmail(currentUser, updateUserParam, bindingResult);

        currentUser.update(
                updateUserParam.getEmail(),
                updateUserParam.getUsername(),
                updateUserParam.getPassword(),
                updateUserParam.getBio(),
                updateUserParam.getImage()
        );

        userRepository.save(currentUser);

        return userService.findById(currentUser.getId())
                .map(userData -> createResponse(userData, authorization))
                .orElseThrow(ResourceNotFoundException::new);
    }

    private void checkUniquenessOfUsernameAndEmail(User currentUser, UpdateUserParam updateUserParam, BindingResult bindingResult) {
        if(!"".equals(updateUserParam.getUsername())) {
            Optional<User> byUsername = userRepository.findByUsername(updateUserParam.getUsername());

            if (byUsername.isPresent() && ! byUsername.get().equals(currentUser)) {
                bindingResult.rejectValue("username", "DUPLICATED", "username already exist");
            }
        }

        if (!"".equals(updateUserParam.getEmail())) {
            Optional<User> byEmail = userRepository.findByEmail(updateUserParam.getEmail());

            if (byEmail.isPresent() && !byEmail.get().equals(currentUser)) {
                bindingResult.rejectValue("email", "DUPLICATED", "email already exist");
            }
        }

        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }
    }

    private ResponseEntity<?> createResponse(UserData userData, String authorization) {
        return ResponseEntity.ok(Map.of("user", new UserWithToken(userData, authorization.split(" ")[1])));
    }

    @Getter
    @JsonRootName("user")
    @NoArgsConstructor
    static class UpdateUserParam {

        @Email(message = "should be an email")
        private String email = "";

        private String password = "";
        private String username = "";
        private String bio = "";
        private String image = "";
    }

}
