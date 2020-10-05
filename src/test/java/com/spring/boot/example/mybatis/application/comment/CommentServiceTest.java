package com.spring.boot.example.mybatis.application.comment;

import com.spring.boot.example.mybatis.application.CommentService;
import com.spring.boot.example.mybatis.application.data.CommentData;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.comment.Comment;
import com.spring.boot.example.mybatis.core.comment.CommentRepository;
import com.spring.boot.example.mybatis.core.user.FollowRelation;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisArticleRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisCommentRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@MybatisTest
@ExtendWith(SpringExtension.class)
@Import({CommentService.class, MyBatisUserRepository.class, MyBatisArticleRepository.class, MyBatisCommentRepository.class})
public class CommentServiceTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ArticleRepository articleRepository;

    private User user;

    @BeforeEach
    void before() {
        user = createUserStub();
        userRepository.save(user);
    }

    @Test
    void testReadCommentSuccess() {
        Comment comment = createCommentStub();
        commentRepository.save(comment);

        Optional<CommentData> optionalCommentData = commentService.findById(comment.getId(), user);

        assertThat(optionalCommentData).isNotEmpty();
        assertSoftly(softly -> {
            CommentData commentData = optionalCommentData.get();
            softly.assertThat(commentData.getProfileData().getUsername()).isEqualTo(user.getUsername());
        });
    }

    @Test
    void testReadCommentsOfArticle() {
        Article article = new Article("title", "desc", "body", new String[] {"java"}, user.getId());
        articleRepository.save(article);

        User otherUser = new User("user2@email.com", "user2", "123", "", "");
        userRepository.save(otherUser);
        userRepository.saveRelation(new FollowRelation(user.getId(), otherUser.getId()));

        Comment commentOne = new Comment("content1", user.getId(), article.getId());
        commentRepository.save(commentOne);
        Comment commentTwo = new Comment("content2", otherUser.getId(), article.getId());
        commentRepository.save(commentTwo);

        List<CommentData> comments = commentService.findByArticleId(article.getId(), user);

        assertThat(comments).hasSize(2);
    }

    private User createUserStub() {
        return new User("aisensiy@test.com", "aisensiy", "123", "", "");
    }

    private Comment createCommentStub() {
        return new Comment("content", user.getId(), "123");
    }

}
