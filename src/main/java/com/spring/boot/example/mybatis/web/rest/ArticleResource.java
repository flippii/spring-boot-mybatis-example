package com.spring.boot.example.mybatis.web.rest;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.spring.boot.example.mybatis.application.ArticleService;
import com.spring.boot.example.mybatis.application.data.ArticleData;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.web.rest.exception.NoAuthorizationException;
import com.spring.boot.example.mybatis.web.rest.exception.ResourceNotFoundException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/articles/{slug}")
public class ArticleResource {

    private final ArticleService articleService;
    private final ArticleRepository articleRepository;

    @GetMapping
    @ApiOperation(value = "", authorizations = { @Authorization(value="JWT") })
    public ResponseEntity<?> article(@PathVariable("slug") String slug,
                                     @AuthenticationPrincipal User user) {

        return articleService.findBySlug(slug, user)
                .map(this::articleResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @PutMapping
    public ResponseEntity<?> updateArticle(@PathVariable("slug") String slug,
                                           @AuthenticationPrincipal User user,
                                           @Valid @RequestBody UpdateArticleParam updateArticleParam) {

        return articleRepository.findBySlug(slug)
                .map(article -> {
                    if (!articleService.canWriteArticle(user, article)) {
                        throw new NoAuthorizationException();
                    }

                    article.update(
                            updateArticleParam.getTitle(),
                            updateArticleParam.getDescription(),
                            updateArticleParam.getBody()
                    );

                    articleRepository.save(article);

                    return articleResponse(articleService.findBySlug(slug, user).get());
                })
                .orElseThrow(ResourceNotFoundException::new);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteArticle(@PathVariable("slug") String slug,
                                           @AuthenticationPrincipal User user) {

        return articleRepository.findBySlug(slug)
                .map(article -> {
                    if (!articleService.canWriteArticle(user, article)) {
                        throw new NoAuthorizationException();
                    }

                    articleRepository.remove(article);

                    return ResponseEntity.noContent().build();
                })
                .orElseThrow(ResourceNotFoundException::new);
    }

    private ResponseEntity<?> articleResponse(ArticleData articleData) {
        return ResponseEntity.ok(Map.of("article", articleData));
    }

    @Getter
    @NoArgsConstructor
    @JsonRootName("article")
    static class UpdateArticleParam {

        private String title = "";
        private String body = "";
        private String description = "";

    }

}
