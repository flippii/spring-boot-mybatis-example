package com.spring.boot.example.mybatis.infrastructure.comment;

import com.spring.boot.example.mybatis.core.comment.Comment;
import com.spring.boot.example.mybatis.core.comment.CommentRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisCommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@ExtendWith(SpringExtension.class)
@Import({MyBatisCommentRepository.class})
public class MyBatisCommentRepositoryTest {

    private static final String ARTICLE_ID = "456";

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void testCreateAndFetchCommentSuccess() {
        Comment comment = createCommentStub();
        commentRepository.save(comment);

        Optional<Comment> optionalComment = commentRepository.findById(ARTICLE_ID, comment.getId());

        assertThat(optionalComment).isNotEmpty();
        assertThat(optionalComment.get()).isEqualToComparingFieldByField(comment);
    }

    private Comment createCommentStub() {
        return new Comment("content", "123", ARTICLE_ID);
    }

}
