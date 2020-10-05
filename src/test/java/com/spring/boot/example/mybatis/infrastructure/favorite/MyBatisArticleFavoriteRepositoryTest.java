package com.spring.boot.example.mybatis.infrastructure.favorite;

import com.spring.boot.example.mybatis.core.favorite.ArticleFavorite;
import com.spring.boot.example.mybatis.core.favorite.ArticleFavoriteRepository;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisArticleFavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@ExtendWith(SpringExtension.class)
@Import({MyBatisArticleFavoriteRepository.class})
public class MyBatisArticleFavoriteRepositoryTest {

    private static final String ARTICLE_ID = "123";
    private static final String USER_ID = "456";

    @Autowired
    private ArticleFavoriteRepository articleFavoriteRepository;

    @Autowired
    private ArticleFavoriteMapper articleFavoriteMapper;

    @Test
    void testSaveAndFetchArticleFavoriteSuccess() {
        ArticleFavorite articleFavorite = createArticleFavoriteStub();
        articleFavoriteRepository.save(articleFavorite);

        assertThat(articleFavoriteMapper.find(ARTICLE_ID, USER_ID))
                .isNotNull()
                .isEqualToComparingFieldByField(articleFavorite);
    }

    @Test
    void testRemoveFavoriteSuccess() {
        ArticleFavorite articleFavorite = createArticleFavoriteStub();
        articleFavoriteRepository.save(articleFavorite);
        articleFavoriteRepository.remove(articleFavorite);

        assertThat(articleFavoriteRepository.find(ARTICLE_ID, USER_ID)).isEmpty();
    }

    private ArticleFavorite createArticleFavoriteStub() {
        return new ArticleFavorite(ARTICLE_ID, USER_ID);
    }

}
