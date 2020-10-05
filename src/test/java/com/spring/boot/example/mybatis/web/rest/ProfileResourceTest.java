package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.ProfileService;
import com.spring.boot.example.mybatis.application.data.ProfileData;
import com.spring.boot.example.mybatis.configuration.JacksonConfiguration;
import com.spring.boot.example.mybatis.configuration.SecurityConfiguration;
import com.spring.boot.example.mybatis.core.user.FollowRelation;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.security.DomainUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileResource.class)
@Import({SecurityConfiguration.class, DomainUserDetailsService.class, JacksonConfiguration.class, TestConfiguration.class})
class ProfileResourceTest extends TestWithCurrentUser {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    private User anotherUser;
    private ProfileData profileData;

    @BeforeEach
    void before() {
        super.before();

        anotherUser = new User("username@test.com", "username", "123", "", "");

        profileData = ProfileData.builder()
                .id(anotherUser.getId())
                .username(anotherUser.getUsername())
                .bio(anotherUser.getBio())
                .image(anotherUser.getImage())
                .following(false)
                .build();

        given(userRepository.findByUsername(eq(anotherUser.getUsername()))).willReturn(Optional.of(anotherUser));
    }

    @Test
    void testGetUserProfileSuccess() throws Exception {
        given(profileService.findByUsername(eq(profileData.getUsername()), any())).willReturn(Optional.of(profileData));

        mockMvc.perform(get("/api/profiles/" + profileData.getUsername())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.username").value(profileData.getUsername()));
    }

    @Test
    void testGetFollowUserSuccess() throws Exception {
        given(profileService.findByUsername(eq(profileData.getUsername()), eq(currentUser))).willReturn(Optional.of(profileData));

        mockMvc.perform(post("/api/profiles/" + anotherUser.getUsername() + "/follow")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userRepository).saveRelation(new FollowRelation(currentUser.getId(), anotherUser.getId()));
    }

    @Test
    void testUnfollowUserSuccess() throws Exception {
        FollowRelation followRelation = new FollowRelation(currentUser.getId(), anotherUser.getId());

        given(userRepository.findRelation(eq(currentUser.getId()), eq(anotherUser.getId()))).willReturn(Optional.of(followRelation));
        given(profileService.findByUsername(eq(profileData.getUsername()), eq(currentUser))).willReturn(Optional.of(profileData));

        mockMvc.perform(delete("/api/profiles/" + anotherUser.getUsername() + "/follow")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userRepository).removeRelation(followRelation);
    }

}
