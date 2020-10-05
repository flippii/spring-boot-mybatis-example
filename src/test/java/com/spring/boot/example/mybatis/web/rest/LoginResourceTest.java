package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.MyBatisWithSpringBootApplication;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.spring.boot.example.mybatis.utils.MvcContentMapper.writeValueAsBytes;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = MyBatisWithSpringBootApplication.class)
public class LoginResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testLoginSuccess() throws Exception {
        String email = "john@jacob.com";
        String username = "johnjacob2";
        String password = "123";
        String defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

        User user = createUserStub(email, username, password, defaultAvatar);

        userRepository.save(user);

        Map<String, Object> loginParam = prepareLoginParameter(email, password);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(loginParam)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.username").value(username))
                .andExpect(jsonPath("$.user.bio").value(""))
                .andExpect(jsonPath("$.user.image").value(defaultAvatar))
                .andExpect(jsonPath("$.user.token").isString());
    }

    @Test
    void testFailLoginWithWrongPassword() throws Exception {
        String email = "john3@jacob.com";
        String username = "johnjacob3";
        String password = "123";
        String defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

        User user = createUserStub(email, username, password, defaultAvatar);

        userRepository.save(user);

        Map<String, Object> loginParam = prepareLoginParameter(email, "wrong");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(loginParam)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.user.token").doesNotExist());
    }

    private User createUserStub(String email, String username, String password, String defaultAvatar) {
        return new User(
                email,
                username,
                passwordEncoder.encode(password),
                "",
                defaultAvatar);
    }

    private Map<String, Object> prepareLoginParameter(String email, String password) {
        return Map.of("user", Map.of("email", email, "password", password));
    }

}
