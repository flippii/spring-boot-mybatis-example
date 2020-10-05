package com.spring.boot.example.mybatis.web.rest;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.spring.boot.example.mybatis.application.ArticleService;
import com.spring.boot.example.mybatis.application.Page;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.web.rest.exception.InvalidRequestException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/articles")
public class ArticlesResource {

    private final ArticleService articleService;
    private final ArticleRepository articleRepository;

    @PostMapping
    public ResponseEntity<?> createArticle(@Valid @RequestBody ArticleParam articleParam,
                                           @AuthenticationPrincipal User user,
                                           BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }

        Article article = new Article(
                articleParam.getTitle(),
                articleParam.getDescription(),
                articleParam.getBody(),
                articleParam.getTagList(),
                UUID.randomUUID().toString());

        articleRepository.save(article);

        return ResponseEntity.ok(Map.of("article", articleService.findById(article.getId(), user)));
    }

    @GetMapping(path = "feed")
    public ResponseEntity<?> feed(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                  @RequestParam(value = "limit", defaultValue = "20") int limit,
                                  @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(articleService.findUserFeed(user, new Page(offset, limit)));
    }

    @GetMapping
    public ResponseEntity<?> articles(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                      @RequestParam(value = "limit", defaultValue = "10") int limit,
                                      @RequestParam(value = "tag", required = false) String tag,
                                      @RequestParam(value = "favorited", required = false) String favorited,
                                      @RequestParam(value = "author", required = false) String author,
                                      @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(articleService.findRecentArticles(tag, author, favorited, new Page(offset, limit), user));
    }

}

@Getter
@JsonRootName("article")
@NoArgsConstructor
class ArticleParam {

    @NotBlank(message = "can't be empty")
    private String title;

    @NotBlank(message = "can't be empty")
    private String description;

    @NotBlank(message = "can't be empty")
    private String body;
    private String[] tagList;

}
