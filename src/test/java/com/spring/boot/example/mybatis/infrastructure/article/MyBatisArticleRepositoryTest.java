package com.spring.boot.example.mybatis.infrastructure.article;

import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisArticleRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisUserRepository;
import org.junit.jupiter.api.BeforeEach;
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
@Import({MyBatisArticleRepository.class, MyBatisUserRepository.class})
public class MyBatisArticleRepositoryTest {

    private static final String TITLE = "test";
    private static final String DESCRIPTION = "desc";
    private static final String BODY = "body";
    private static final String[] TAGS = new String[]{"java", "spring"};

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    private Article article;

    @BeforeEach
    void before() {
        User user = createUserStub();
        userRepository.save(user);
        article = createArticleStub(user);
    }

    @Test
    void testCreateAndFetchArticleSuccess() {
        articleRepository.save(article);

        Optional<Article> optionalArticle = articleRepository.findById(article.getId());

        assertThat(optionalArticle).isNotEmpty();
        assertThat(optionalArticle.get()).isEqualToComparingFieldByField(article);
    }

    @Test
    void testUpdateAndFetchArticleSuccess() {
        articleRepository.save(article);

        String newTitle = "new test 2";
        article.update(newTitle, "", "");
        articleRepository.save(article);
        System.out.println(article.getSlug());
        Optional<Article> optionalArticle = articleRepository.findBySlug(article.getSlug());

        assertThat(optionalArticle).isNotEmpty();
        assertThat(optionalArticle.get().getTitle()).isEqualTo(newTitle);
        assertThat(optionalArticle.get().getBody()).isNotEmpty();
    }

    @Test
    void testDeleteArticle() {
        articleRepository.save(article);
        articleRepository.remove(article);

        assertThat(articleRepository.findById(article.getId())).isEmpty();
    }

    private User createUserStub() {
        return new User("aisensiy@gmail.com", "aisensiy", "123", "bio", "default");
    }

    private Article createArticleStub(User user) {
        return new Article(TITLE, DESCRIPTION, BODY, TAGS, user.getId());
    }

}
