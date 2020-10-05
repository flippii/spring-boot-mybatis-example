package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.ProfileService;
import com.spring.boot.example.mybatis.application.data.ProfileData;
import com.spring.boot.example.mybatis.core.user.FollowRelation;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.web.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/profiles/{username}")
public class ProfileResource {

    private final ProfileService profileService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> profile(@PathVariable("username") String username,
                                    @AuthenticationPrincipal User user) {

        return profileService.findByUsername(username, user)
                .map(this::createResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @PostMapping(path = "follow")
    public ResponseEntity<?> follow(@PathVariable("username") String username,
                                    @AuthenticationPrincipal User user) {

        return userRepository.findByUsername(username)
                .map(targetUser -> {
                    FollowRelation followRelation = new FollowRelation(user.getId(), targetUser.getId());
                    userRepository.saveRelation(followRelation);
                    return createResponse(profileService.findByUsername(username, user).get());
                })
                .orElseThrow(ResourceNotFoundException::new);
    }

    @DeleteMapping(path = "follow")
    public ResponseEntity<?> unfollow(@PathVariable("username") String username,
                                      @AuthenticationPrincipal User user) {

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isPresent()) {
            User targetUser = optionalUser.get();

            return userRepository.findRelation(user.getId(), targetUser.getId())
                    .map(followRelation -> {
                        userRepository.removeRelation(followRelation);
                        return createResponse(profileService.findByUsername(username, user).get());
                    })
                    .orElseThrow(ResourceNotFoundException::new);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    private ResponseEntity<?> createResponse(ProfileData profileData) {
        return ResponseEntity.ok(Map.of("profile", profileData));
    }

}
