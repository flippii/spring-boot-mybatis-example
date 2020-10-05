package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.ArticleService;
import com.spring.boot.example.mybatis.application.data.ArticleData;
import com.spring.boot.example.mybatis.application.data.ProfileData;
import com.spring.boot.example.mybatis.configuration.JacksonConfiguration;
import com.spring.boot.example.mybatis.configuration.SecurityConfiguration;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.security.DomainUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static com.spring.boot.example.mybatis.utils.MvcContentMapper.writeValueAsBytes;
import static com.spring.boot.example.mybatis.web.rest.ArticleFixtures.createArticleAndUserData;
import static com.spring.boot.example.mybatis.web.rest.ArticleFixtures.createArticleData;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ArticleResource.class)
@Import({SecurityConfiguration.class, DomainUserDetailsService.class, JacksonConfiguration.class, TestConfiguration.class})
public class ArticleResourceTest extends TestWithCurrentUser {

    private static final String SLUG = "test-new-article";
    private static final String TITLE = "new-title";
    private static final String BODY = "new body";
    private static final String DESCRIPTION = "new description";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private ArticleRepository articleRepository;

    @Test
    void testReadArticleSuccess() throws Exception {
        LocalDate time = LocalDate.now();
        ArticleData articleData = createArticleData(SLUG, time);

        given(articleService.findBySlug(anyString(), eq(currentUser))).willReturn(Optional.of(articleData));

        mockMvc.perform(get("/api/articles/" + SLUG)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.article.slug").value(SLUG))
                .andExpect(jsonPath("$.article.body").value(articleData.getBody()))
                .andExpect(jsonPath("$.article.createdAt").value(time.toString()));
    }

    @Test
    void test404IfArticleNotFound() throws Exception {
        given(articleService.findBySlug(anyString(), eq(currentUser))).willReturn(Optional.empty());

        mockMvc.perform(get("/api/articles/not-found")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateArticleContentSuccess() throws Exception {
        Map<String, Object> updateParam = prepareUpdateParam(TITLE, BODY, DESCRIPTION);

        Article article = createArticleStub(currentUser);

        ArticleData articleData = createArticleAndUserData(article, currentUser);

        given(articleRepository.findBySlug(anyString())).willReturn(Optional.of(article));
        given(articleService.findBySlug(eq(article.getSlug()), eq(currentUser))).willReturn(Optional.of(articleData));
        given(articleService.canWriteArticle(eq(currentUser), eq(article))).willReturn(true);

        mockMvc.perform(put("/api/articles/" + article.getSlug())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(updateParam)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.slug").value(article.getSlug()));
    }

    @Test
    void testGet403IfNotAuthorToUpdateArticle() throws Exception {
        Map<String, Object> updateParam = prepareUpdateParam(TITLE, BODY, DESCRIPTION);

        User anotherUser = createAnotherUserStub();
        Article article = createArticleStub(anotherUser);

        LocalDate time = LocalDate.now();
        ArticleData articleData = ArticleData.builder()
                .id(article.getId())
                .slug(article.getSlug())
                .title(article.getTitle())
                .description(article.getDescription())
                .body(article.getBody())
                .favorited(false)
                .favoritesCount(0)
                .createdAt(time)
                .updatedAt(time)
                .tagList(List.of("joda"))
                .profileData(ProfileData.builder()
                        .id(anotherUser.getId())
                        .username(anotherUser.getUsername())
                        .bio(anotherUser.getBio())
                        .image(anotherUser.getImage())
                        .following(false)
                        .build())
                .build();

        given(articleRepository.findBySlug(eq(article.getSlug()))).willReturn(Optional.of(article));
        given(articleService.findBySlug(eq(article.getSlug()), eq(currentUser))).willReturn(Optional.of(articleData));

        mockMvc.perform(put("/api/articles/" + article.getSlug())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(updateParam)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteArticleSuccess() throws Exception {
        Article article = createArticleStub(currentUser);

        given(articleRepository.findBySlug(eq(article.getSlug()))).willReturn(Optional.of(article));
        given(articleService.canWriteArticle(eq(currentUser), eq(article))).willReturn(true);

        mockMvc.perform(delete("/api/articles/" + article.getSlug())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(articleRepository).remove(eq(article));
    }

    @Test
    void test403IfNotAuthorDeleteArticle() throws Exception {
        User anotherUser = createAnotherUserStub();
        Article article = createArticleStub(anotherUser);

        given(articleRepository.findBySlug(eq(article.getSlug()))).willReturn(Optional.of(article));

        mockMvc.perform(delete("/api/articles/" + article.getSlug())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private Map<String, Object> prepareUpdateParam(final String title, final String body, final String description) {
        return Map.of("article",
                Map.of("title", title, "body", body, "description", description));
    }

    private User createAnotherUserStub() {
        return new User(
                "test@test.com",
                "test",
                "123123",
                "",
                ""
        );
    }

    private Article createArticleStub(User user) {
        return new Article(
                TITLE,
                DESCRIPTION,
                BODY,
                new String[] {"java", "spring", "jpa"},
                user.getId()
        );
    }

}
