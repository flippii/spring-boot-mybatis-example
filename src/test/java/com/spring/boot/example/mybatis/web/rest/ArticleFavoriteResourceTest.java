package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.ArticleService;
import com.spring.boot.example.mybatis.application.data.ArticleData;
import com.spring.boot.example.mybatis.application.data.ProfileData;
import com.spring.boot.example.mybatis.configuration.JacksonConfiguration;
import com.spring.boot.example.mybatis.configuration.SecurityConfiguration;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.favorite.ArticleFavorite;
import com.spring.boot.example.mybatis.core.favorite.ArticleFavoriteRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleFavoriteResource.class)
@Import({SecurityConfiguration.class, DomainUserDetailsService.class, JacksonConfiguration.class, TestConfiguration.class})
public class ArticleFavoriteResourceTest extends TestWithCurrentUser {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleFavoriteRepository articleFavoriteRepository;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ArticleService articleService;

    private Article article;

    @BeforeEach
    void before() {
        super.before();

        User anotherUser = createAnotherUserStub();
        article = createArticleStub(anotherUser);

        given(articleRepository.findBySlug(eq(article.getSlug()))).willReturn(Optional.of(article));

        ArticleData articleData = ArticleData.builder()
                .id(article.getId())
                .slug(article.getSlug())
                .title(article.getTitle())
                .description(article.getDescription())
                .body(article.getBody())
                .favorited(true)
                .favoritesCount(1)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .profileData(ProfileData.builder()
                        .id(anotherUser.getId())
                        .username(anotherUser.getUsername())
                        .bio(anotherUser.getBio())
                        .image(anotherUser.getImage())
                        .following(false)
                        .build())
                .build();

        given(articleService.findBySlug(eq(articleData.getSlug()), eq(currentUser))).willReturn(Optional.of(articleData));
    }

    @Test
    void testFavouriteAnArticleSuccess() throws Exception {
        mockMvc.perform(post("/api/articles/" + article.getSlug() + "/favourite")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.id").value(article.getId()));

        verify(articleFavoriteRepository).save(any());
    }

    @Test
    void testUnFavouriteAnArticleSuccess() throws Exception {
        ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), currentUser.getId());

        given(articleFavoriteRepository.find(eq(article.getId()), eq(currentUser.getId())))
                .willReturn(Optional.of(articleFavorite));

        mockMvc.perform(delete("/api/articles/" + article.getSlug() + "/favourite")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article.id").value(article.getId()));

        verify(articleFavoriteRepository).remove(articleFavorite);
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
                "title",
                "desc",
                "body",
                new String[] {"java", "spring", "jpa"},
                user.getId()
        );
    }

}
