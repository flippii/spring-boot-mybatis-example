package com.spring.boot.example.mybatis.application;

import com.spring.boot.example.mybatis.application.data.ProfileData;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.UserReadService;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.UserRelationshipReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserReadService userReadService;
    private final UserRelationshipReadService userRelationshipReadService;

    public Optional<ProfileData> findByUsername(String username, User currentUser) {
        return Optional.ofNullable(userReadService.findByUsername(username))
                .map(userData ->
                    new ProfileData(
                         userData.getId(),
                         userData.getUsername(),
                         userData.getBio(),
                         userData.getImage(),
                         userRelationshipReadService.isUserFollowing(currentUser.getId(), userData.getId())
                    )
                );
    }

}
