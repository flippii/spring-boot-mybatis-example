package com.spring.boot.example.mybatis.core.article;

import java.util.Optional;

public interface ArticleRepository {

    Optional<Article> findBySlug(String slug);

    Optional<Article> findById(String id);

    void save(Article article);

    void remove(Article article);

}
