package com.spring.boot.example.mybatis.infrastructure.article;

import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.ArticleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
public class ArticleRepositoryTransactionTest {

    private static final String TITLE = "test";
    private static final String DESCRIPTION = "desc";
    private static final String BODY = "body";

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleMapper articleMapper;

    @Test
    void testSaveArticleTransactional() {
        User user = createUserStub();
        userRepository.save(user);

        Article article = createArticleStub(new String[]{"java", "spring"}, user);
        articleRepository.save(article);

        Article anotherArticle = createArticleStub(new String[]{"java", "spring", "other"}, user);

        Throwable thrown = catchThrowable(() -> articleRepository.save(anotherArticle));

        assertThat(thrown).isInstanceOf(DuplicateKeyException.class);
        assertThat(articleMapper.existsTag("other")).isFalse();
    }

    private User createUserStub() {
        return new User("aisensiy@gmail.com", "aisensiy", "123", "bio", "default");
    }

    private Article createArticleStub(String[] tags, User user) {
        return new Article(TITLE, DESCRIPTION, BODY, tags, user.getId());
    }

}
