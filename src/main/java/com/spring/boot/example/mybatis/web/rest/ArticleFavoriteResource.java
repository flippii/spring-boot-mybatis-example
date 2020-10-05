package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.ArticleService;
import com.spring.boot.example.mybatis.application.data.ArticleData;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.favorite.ArticleFavorite;
import com.spring.boot.example.mybatis.core.favorite.ArticleFavoriteRepository;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.web.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/articles/{slug}/favourite")
public class ArticleFavoriteResource {

    private final ArticleFavoriteRepository articleFavoriteRepository;
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

    @PostMapping
    public ResponseEntity<?> favouriteArticle(@PathVariable("slug") String slug,
                                              @AuthenticationPrincipal User user) {

        Article article = getArticle(slug);
        ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
        articleFavoriteRepository.save(articleFavorite);
        return responseArticleData(articleService.findBySlug(slug, user).get());
    }

    @DeleteMapping
    public ResponseEntity<?> unFavouriteArticle(@PathVariable("slug") String slug,
                                                @AuthenticationPrincipal User user) {

        Article article = getArticle(slug);
        articleFavoriteRepository.find(article.getId(), user.getId())
                .ifPresent(articleFavoriteRepository::remove);
        return responseArticleData(articleService.findBySlug(slug, user).get());
    }

    private Article getArticle(String slug) {
        return articleRepository.findBySlug(slug)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private ResponseEntity<?> responseArticleData(final ArticleData articleData) {
        return ResponseEntity.ok(Map.of("article", articleData));
    }

}
