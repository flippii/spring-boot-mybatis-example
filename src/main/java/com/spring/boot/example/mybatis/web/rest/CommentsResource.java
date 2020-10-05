package com.spring.boot.example.mybatis.web.rest;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.spring.boot.example.mybatis.application.CommentService;
import com.spring.boot.example.mybatis.application.data.CommentData;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.comment.Comment;
import com.spring.boot.example.mybatis.core.comment.CommentRepository;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.web.rest.exception.InvalidRequestException;
import com.spring.boot.example.mybatis.web.rest.exception.NoAuthorizationException;
import com.spring.boot.example.mybatis.web.rest.exception.ResourceNotFoundException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/articles/{slug}/comments")
public class CommentsResource {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<?> comments(@PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
        Article article = findArticle(slug);
        List<CommentData> comments = commentService.findByArticleId(article.getId(), user);
        return ResponseEntity.ok(Map.of("comments", comments));
    }

    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable("slug") String slug,
                                           @AuthenticationPrincipal User user,
                                           @Valid @RequestBody NewCommentParam newCommentParam,
                                           BindingResult bindingResult) {

        Article article = findArticle(slug);

        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }

        Comment comment = new Comment(
                newCommentParam.getBody(),
                user.getId(),
                article.getId()
        );

        commentRepository.save(comment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("comment", commentService.findById(comment.getId(), user).get()));
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<?> deleteComment(@PathVariable("slug") String slug,
                                           @PathVariable("id") String commentId,
                                           @AuthenticationPrincipal User user) {

        Article article = findArticle(slug);

        return commentRepository.findById(article.getId(), commentId)
                .map(comment -> {
                    if (!commentService.canWriteComment(user, article, comment)) {
                        throw new NoAuthorizationException();
                    }

                    commentRepository.remove(comment);

                    return ResponseEntity.noContent().build();
                }).orElseThrow(ResourceNotFoundException::new);
    }

    private Article findArticle(String slug) {
        return articleRepository.findBySlug(slug)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Getter
    @NoArgsConstructor
    @JsonRootName("comment")
    static class NewCommentParam {

        @NotBlank(message = "can't be empty")
        private String body;

    }

}
