package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.ArticleService;
import com.spring.boot.example.mybatis.application.Page;
import com.spring.boot.example.mybatis.application.data.ArticleData;
import com.spring.boot.example.mybatis.application.data.ArticleDataList;
import com.spring.boot.example.mybatis.application.data.ProfileData;
import com.spring.boot.example.mybatis.configuration.JacksonConfiguration;

import com.spring.boot.example.mybatis.configuration.SecurityConfiguration;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.infrastructure.security.DomainUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.spring.boot.example.mybatis.utils.MvcContentMapper.writeValueAsBytes;
import static com.spring.boot.example.mybatis.web.rest.ArticleFixtures.createArticleData;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ArticlesResource.class)
@Import({SecurityConfiguration.class, DomainUserDetailsService.class, JacksonConfiguration.class, TestConfiguration.class})
class ArticlesResourceTest extends TestWithCurrentUser {

    private static final String TITLE = "How to train your dragon";
    private static final String SLUG = "how-to-train-your-dragon";
    private static final String DESCRIPTION = "Ever wonder how?";
    private static final String BODY = "You have to believe";
    private static final String[] TAGS = {"reactjs", "angularjs", "dragons"};

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ArticleService articleService;

    @Test
    void testCreateArticleSuccess() throws Exception {
        Map<String, Object> createParam = prepareCreateParam(DESCRIPTION);

        LocalDate time = LocalDate.now();
        ArticleData articleData = ArticleData.builder()
                .id("123")
                .slug(SLUG)
                .title(TITLE)
                .description(DESCRIPTION)
                .body(BODY)
                .favorited(false)
                .favoritesCount(0)
                .createdAt(time)
                .updatedAt(time)
                .tagList(List.of(TAGS))
                .profileData(ProfileData.builder()
                        .id("userId")
                        .username(currentUser.getUsername())
                        .bio(currentUser.getBio())
                        .image(currentUser.getImage())
                        .following(false)
                        .build())
                .build();

        given(articleService.findById(anyString(), eq(currentUser))).willReturn(Optional.of(articleData));

        mockMvc.perform(post("/api/articles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(createParam)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.title").value(articleData.getTitle()))
                .andExpect(jsonPath("$.article.body").value(articleData.getBody()))
                .andExpect(jsonPath("$.article.favoritesCount").value(articleData.getFavoritesCount()))
                .andExpect(jsonPath("$.article.favorited").value(articleData.isFavorited()))
                .andExpect(jsonPath("$.article.author.username").value(currentUser.getUsername()))
                .andExpect(jsonPath("$.article.author.id").doesNotExist());

        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void testGetErrorMessageWithWrongParameter() throws Exception {
        Map<String, Object> createParam = prepareCreateParam("");

        mockMvc.perform(post("/api/articles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(createParam)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.description").value("can't be empty"));
    }

    private Map<String, Object> prepareCreateParam(String description) {
        return Map.of("article",
                Map.of("title", TITLE, "body", BODY, "description", description, "tagList", TAGS));
    }

    @Test
    void testGetDefaultArticleList() throws Exception {
        ArticleDataList articleDataList = new ArticleDataList(List.of(createArticleData("slug", LocalDate.now())), 1);

        given(articleService.findRecentArticles(anyString(), anyString(), anyString(), eq(new Page(0, 20)), eq(currentUser)))
                .willReturn(articleDataList);

        mockMvc.perform(get("/api/articles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetFeeds401WithoutLogin() throws Exception {
        mockMvc.perform(get("/api/articles/feed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetFeedsSuccess() throws Exception {
        ArticleDataList articleDataList = new ArticleDataList(List.of(createArticleData("slug", LocalDate.now())), 1);

        given(articleService.findRecentArticles(anyString(), anyString(), anyString(), eq(new Page(0, 20)), eq(currentUser)))
                .willReturn(articleDataList);

        mockMvc.perform(get("/api/articles/feed")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
