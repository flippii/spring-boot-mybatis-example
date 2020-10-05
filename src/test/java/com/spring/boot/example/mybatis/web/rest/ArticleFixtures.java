package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.data.ArticleData;
import com.spring.boot.example.mybatis.application.data.ProfileData;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.user.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ArticleFixtures {

    static ArticleData createArticleData(String slug, LocalDate time) {
        return ArticleData.builder()
                .id(UUID.randomUUID().toString())
                .slug(slug)
                .title("Test New Article")
                .description("Desc")
                .body("Body")
                .favorited(false)
                .favoritesCount(0)
                .createdAt(time)
                .updatedAt(time)
                .tagList(List.of("joda"))
                .profileData(ProfileData.builder()
                        .id(UUID.randomUUID().toString())
                        .username("johnjacob")
                        .bio("")
                        .image("https://static.productionready.io/images/smiley-cyrus.jpg")
                        .following(false)
                        .build())
                .build();
    }

    static ArticleData createArticleAndUserData(Article article, User user) {
        return ArticleData.builder()
                .id(article.getId())
                .slug(article.getSlug())
                .title(article.getTitle())
                .description(article.getDescription())
                .body(article.getBody())
                .favorited(false)
                .favoritesCount(0)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .tagList(List.of("joda"))
                .profileData(ProfileData.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .bio(user.getBio())
                        .image(user.getImage())
                        .following(false)
                        .build())
                .build();
    }

}
