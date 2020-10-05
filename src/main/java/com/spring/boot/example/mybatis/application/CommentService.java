package com.spring.boot.example.mybatis.application;

import com.spring.boot.example.mybatis.application.data.CommentData;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.comment.Comment;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.CommentReadService;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.UserRelationshipReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentReadService commentReadService;
    private final UserRelationshipReadService userRelationshipReadService;

    public Optional<CommentData> findById(String id, User user) {
        CommentData comment = commentReadService.findById(id);

        if (comment == null) {
            return Optional.empty();
        } else {
            comment.getProfileData()
                    .setFollowing(userRelationshipReadService.isUserFollowing(user.getId(), comment.getProfileData().getId()));
        }

        return Optional.of(comment);
    }

    public List<CommentData> findByArticleId(String articleId, User user) {
        List<CommentData> comments = commentReadService.findByArticleId(articleId);

        if (!comments.isEmpty() && user != null) {
            Set<String> followingAuthors = userRelationshipReadService.followingAuthors(user.getId(), collectProfileIds(comments));

            comments.forEach(commentData -> {
                if (followingAuthors.contains(commentData.getProfileData().getId())) {
                    commentData.getProfileData().setFollowing(true);
                }
            });
        }

        return comments;
    }

    private List<String> collectProfileIds(List<CommentData> comments) {
        return comments.stream()
                .map(commentData -> commentData.getProfileData().getId())
                .collect(Collectors.toList());
    }

    public boolean canWriteComment(User user, Article article, Comment comment) {
        return user.getId().equals(article.getUserId()) || user.getId().equals(comment.getUserId());
    }

}
