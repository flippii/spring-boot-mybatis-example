package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.UserService;
import com.spring.boot.example.mybatis.application.data.UserData;
import com.spring.boot.example.mybatis.configuration.JacksonConfiguration;
import com.spring.boot.example.mybatis.configuration.SecurityConfiguration;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.UserReadService;
import com.spring.boot.example.mybatis.infrastructure.security.DomainUserDetailsService;
import com.spring.boot.example.mybatis.infrastructure.security.TokenProvider;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserResource.class)
@Import({SecurityConfiguration.class, DomainUserDetailsService.class, UserService.class, JacksonConfiguration.class})
public class UserResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserReadService userReadService;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    void testCreateUserSuccess() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob";
        String defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

        given(tokenProvider.createToken(any())).willReturn("123");

        User user = new User(email, username, "123", "", defaultAvatar);
        UserData userData = new UserData(user.getId(), email, username, "", defaultAvatar);
        given(userReadService.findById(any())).willReturn(userData);

        given(userRepository.findByUsername(eq(username))).willReturn(Optional.empty());
        given(userRepository.findByEmail(eq(email))).willReturn(Optional.empty());

        Map<String, Object> registerParam = prepareRegisterParameter(email, username);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(registerParam)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.username").value(username))
                .andExpect(jsonPath("$.user.bio").value(""))
                .andExpect(jsonPath("$.user.image").value(defaultAvatar))
                .andExpect(jsonPath("$.user.token").value("123"));

        verify(userRepository).save(any());
    }

    @Test
    void testShowErrorMessageForBlankUsername() throws Exception {
        String email = "john@jacob.com";
        String username = "";

        Map<String, Object> registerParam = prepareRegisterParameter(email, username);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(registerParam)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.username[0]").value("can't be empty"));
    }

    @Test
    void testShowErrorMessageForInvalidEmail() throws Exception {
        String email = "johnxjacob.com";
        String username = "johnjacob";

        Map<String, Object> registerParam = prepareRegisterParameter(email, username);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(registerParam)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.email[0]").value("should be an email"));
    }

    @Test
    void testShowErrorForDuplicatedUsername() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob";

        given(userRepository.findByUsername(eq(username)))
                .willReturn(Optional.of(new User(email, username, "123", "bio", "")));
        given(userRepository.findByEmail(any())).willReturn(Optional.empty());

        Map<String, Object> registerParam = prepareRegisterParameter(email, username);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(registerParam)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.username[0]").value("duplicated username"));
    }

    @Test
    void testShowErrorForDuplicatedEmail() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob2";

        given(userRepository.findByEmail(eq(email)))
                .willReturn(Optional.of(new User(email, username, "123", "bio", "")));
        given(userRepository.findByUsername(eq(username))).willReturn(Optional.empty());

        Map<String, Object> registerParam = prepareRegisterParameter(email, username);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(registerParam)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.email[0]").value("duplicated email"));
    }

    private Map<String, Object> prepareRegisterParameter(final String email, final String username) {
        return Map.of("user",
                Map.of("email", email, "password", "johnnyjacob", "username", username));
    }

}
