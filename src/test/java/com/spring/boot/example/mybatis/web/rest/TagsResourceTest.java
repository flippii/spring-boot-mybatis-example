package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.TagsService;
import com.spring.boot.example.mybatis.configuration.JacksonConfiguration;
import com.spring.boot.example.mybatis.configuration.SecurityConfiguration;
import com.spring.boot.example.mybatis.infrastructure.security.DomainUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagsResource.class)
@Import({SecurityConfiguration.class, DomainUserDetailsService.class, JacksonConfiguration.class, TestConfiguration.class})
class TagsResourceTest extends TestWithCurrentUser {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagsService tagsService;

    @Test
    void testGetTagsSuccess() throws Exception {
        List<String> tags = List.of("reactjs", "angularjs", "dragons");

        given(tagsService.allTags()).willReturn(tags);

        mockMvc.perform(get("/api/tags")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0]").value("reactjs"));
    }

}
