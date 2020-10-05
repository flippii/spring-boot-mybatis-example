package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.UserService;
import com.spring.boot.example.mybatis.configuration.JacksonConfiguration;
import com.spring.boot.example.mybatis.configuration.SecurityConfiguration;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.security.DomainUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static com.spring.boot.example.mybatis.utils.MvcContentMapper.writeValueAsBytes;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrentUserResource.class)
@Import({SecurityConfiguration.class, DomainUserDetailsService.class, JacksonConfiguration.class, TestConfiguration.class})
class CurrentUserResourceTest extends TestWithCurrentUser {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testGetCurrentUserWithToken() throws Exception {
        given(userService.findById(any())).willReturn(Optional.of(userData));

        mockMvc.perform(get("/api/user")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(userData.getEmail()))
                .andExpect(jsonPath("$.user.bio").value(userData.getBio()))
                .andExpect(jsonPath("$.user.token").value(token))
                .andExpect(jsonPath("$.user.username").value(userData.getUsername()))
                .andExpect(jsonPath("$.user.image").value(userData.getImage()));
    }

    @Test
    void testGet401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/user")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGet401WithInvalidToken() throws Exception {
        String invalidToken = "invalid token";

        mockMvc.perform(get("/api/user")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateCurrentUserProfile() throws Exception {
        String newEmail = "newemail@example.com";
        String newBio = "updated";
        String newUsername = "newusernamee";

        Map<String, Object> updateParam = updateParam(newEmail, newBio, newUsername);

        given(userRepository.findByUsername(eq(newUsername))).willReturn(Optional.empty());
        given(userRepository.findByEmail(eq(newEmail))).willReturn(Optional.empty());

        given(userService.findById(eq(userData.getId()))).willReturn(Optional.of(userData));

        mockMvc.perform(put("/api/user")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(updateParam)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetErrorIfEmailExistsWhenUpdateUserProfile() throws Exception {
        String newEmail = "newemail@example.com";
        String newBio = "updated";
        String newUsername = "newusernamee";

        Map<String, Object> updateParam = updateParam(newEmail, newBio, newUsername);

        given(userRepository.findByUsername(eq(newUsername))).willReturn(Optional.empty());
        given(userRepository.findByEmail(eq(newEmail))).willReturn(Optional.of(
                new User(newEmail, "username", "123", "", "")
        ));

        given(userService.findById(eq(userData.getId()))).willReturn(Optional.of(userData));

        mockMvc.perform(put("/api/user")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(updateParam)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.email[0]").value("email already exist"));
    }

    private Map<String, Object> updateParam(String email, String bio, String username) {
        return Map.of("user", Map.of("email", email, "bio", bio, "username", username));
    }

    @Test
    void testGet401IfNotLogin() throws Exception {
        mockMvc.perform(put("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(Map.of("user", Map.of()))))
                .andExpect(status().isUnauthorized());
    }

}
