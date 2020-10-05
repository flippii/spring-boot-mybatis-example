package com.spring.boot.example.mybatis.infrastructure.repository;

import com.spring.boot.example.mybatis.core.favorite.ArticleFavorite;
import com.spring.boot.example.mybatis.core.favorite.ArticleFavoriteRepository;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {

    private final ArticleFavoriteMapper articleFavoriteMapper;

    @Override
    public void save(ArticleFavorite articleFavorite) {
        if (articleFavoriteMapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId()) == null) {
            articleFavoriteMapper.insert(articleFavorite);
        }
    }

    @Override
    public Optional<ArticleFavorite> find(String articleId, String userId) {
        return Optional.ofNullable(articleFavoriteMapper.find(articleId, userId));
    }

    @Override
    public void remove(ArticleFavorite articleFavorite) {
        articleFavoriteMapper.delete(articleFavorite);
    }

}
