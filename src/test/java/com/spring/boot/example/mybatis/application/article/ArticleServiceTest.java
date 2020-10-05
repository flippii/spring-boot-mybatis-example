package com.spring.boot.example.mybatis.application.article;

import com.spring.boot.example.mybatis.application.ArticleService;
import com.spring.boot.example.mybatis.application.Page;
import com.spring.boot.example.mybatis.application.data.ArticleData;
import com.spring.boot.example.mybatis.application.data.ArticleDataList;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.favorite.ArticleFavorite;
import com.spring.boot.example.mybatis.core.favorite.ArticleFavoriteRepository;
import com.spring.boot.example.mybatis.core.user.FollowRelation;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisArticleFavoriteRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisArticleRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisUserRepository;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@MybatisTest
@ExtendWith(SpringExtension.class)
@Import({ArticleService.class, MyBatisUserRepository.class, MyBatisArticleRepository.class, MyBatisArticleFavoriteRepository.class})
class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleFavoriteRepository articleFavoriteRepository;

    private User user;
    private Article article;

    @BeforeEach
    void before() {
        user = createUserStub();
        userRepository.save(user);

        article = createArticleStub();
        articleRepository.save(article);
    }

    @Test
    void testFetchArticleSuccess() {
        Optional<ArticleData> optionalRecentArticle = articleService.findById(article.getId(), user);

        assertThat(optionalRecentArticle).isNotEmpty();

        assertSoftly(softly -> {
            ArticleData recentArticle = optionalRecentArticle.get();
            softly.assertThat(recentArticle.getFavoritesCount()).isZero();
            softly.assertThat(recentArticle.isFavorited()).isFalse();
            softly.assertThat(recentArticle.getCreatedAt()).isNotNull();
            softly.assertThat(recentArticle.getUpdatedAt()).isNotNull();
            softly.assertThat(recentArticle.getTagList()).contains("java");
        });
    }

    @Test
    void testGetArticleWithRightFavoriteAndFavoriteCount() {
        User anotherUser = new User("other@test.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        articleFavoriteRepository.save(new ArticleFavorite(article.getId(), anotherUser.getId()));

        Optional<ArticleData> optionalRecentArticle = articleService.findById(article.getId(), anotherUser);

        assertThat(optionalRecentArticle).isNotEmpty();
        assertSoftly(softly -> {
            ArticleData recentArticle = optionalRecentArticle.get();
            softly.assertThat(recentArticle.getFavoritesCount()).isEqualTo(1);
            softly.assertThat(recentArticle.isFavorited()).isTrue();
        });
    }

    @Test
    void testGetDefaultArticleList() {
        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, user.getId(), LocalDate.now().minusWeeks(1));
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = articleService.findRecentArticles(null, null, null, new Page(), user);

        assertSoftly(softly -> {
            softly.assertThat(recentArticles.getCount()).isEqualTo(2);
            softly.assertThat(recentArticles.getArticleDatas())
                    .hasSize(2)
                    .areExactly(1, new Condition<>(ad -> ad.getId().equals(article.getId()), "test article id is equal"));
        });

        ArticleDataList emptyRecentArticles = articleService.findRecentArticles(null, null, null, new Page(2, 10), user);

        assertSoftly(softly -> {
            softly.assertThat(emptyRecentArticles.getCount()).isEqualTo(2);
            softly.assertThat(emptyRecentArticles.getArticleDatas()).isEmpty();
        });
    }

    @Test
    void testQueryArticleByAuthor() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, anotherUser.getId());
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = articleService.findRecentArticles(null, user.getUsername(), null, new Page(), user);

        assertSoftly(softly -> {
            softly.assertThat(recentArticles.getArticleDatas()).hasSize(1);
            softly.assertThat(recentArticles.getCount()).isEqualTo(1);
        });
    }

    @Test
    void testQueryArticleByFavorite() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, anotherUser.getId());
        articleRepository.save(anotherArticle);

        ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), anotherUser.getId());
        articleFavoriteRepository.save(articleFavorite);

        ArticleDataList recentArticles = articleService.findRecentArticles(null, null, anotherUser.getUsername(), new Page(), anotherUser);

        assertSoftly(softly -> {
            softly.assertThat(recentArticles.getArticleDatas()).hasSize(1);
            softly.assertThat(recentArticles.getCount()).isEqualTo(1);

            ArticleData articleData = recentArticles.getArticleDatas().get(0);
            softly.assertThat(articleData.getId()).isEqualTo(article.getId());
            softly.assertThat(articleData.getFavoritesCount()).isEqualTo(1);
            softly.assertThat(articleData.isFavorited()).isTrue();
        });
    }

    @Test
    void testQueryArticleByTag() {
        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, user.getId());
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = articleService.findRecentArticles("spring", null, null, new Page(), user);

        assertSoftly(softly -> {
            softly.assertThat(recentArticles.getArticleDatas()).hasSize(1);
            softly.assertThat(recentArticles.getCount()).isEqualTo(1);
            softly.assertThat(recentArticles.getArticleDatas().get(0).getId()).isEqualTo(article.getId());
        });

        ArticleDataList noTagRecentArticles = articleService.findRecentArticles("notag", null, null, new Page(), user);
        assertThat(noTagRecentArticles.getCount()).isZero();
    }

    @Test
    void testShowFollowingIfUserFollowedAuthor() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
        userRepository.saveRelation(followRelation);

        ArticleDataList recentArticles = articleService.findRecentArticles(null, null, null, new Page(), anotherUser);

        assertSoftly(softly -> {
            softly.assertThat(recentArticles.getCount()).isEqualTo(1);

            ArticleData articleData = recentArticles.getArticleDatas().get(0);
            softly.assertThat(articleData.getProfileData().isFollowing()).isTrue();
        });
    }

    @Test
    void testGetUserFeed() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
        userRepository.saveRelation(followRelation);

        ArticleDataList userFeed = articleService.findUserFeed(user, new Page());

        assertSoftly(softly -> {
            softly.assertThat(userFeed.getCount()).isZero();
            softly.assertThat(userFeed.getArticleDatas()).isEmpty();
        });

        ArticleDataList anotherUserFeed = articleService.findUserFeed(anotherUser, new Page());

        assertSoftly(softly -> {
            softly.assertThat(anotherUserFeed.getCount()).isEqualTo(1);

            ArticleData articleData = anotherUserFeed.getArticleDatas().get(0);
            assertThat(articleData.getProfileData().isFollowing()).isTrue();
        });
    }

    private User createUserStub() {
        return new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
    }

    private Article createArticleStub() {
        return new Article("test", "desc", "body", new String[]{"java", "spring"}, user.getId(), LocalDate.now());
    }

}
