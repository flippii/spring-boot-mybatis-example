package com.spring.boot.example.mybatis.infrastructure.repository;

import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisArticleRepository implements ArticleRepository {

    private final ArticleMapper articleMapper;

    @Override
    public Optional<Article> findBySlug(String slug) {
        return Optional.ofNullable(articleMapper.findBySlug(slug));
    }

    @Override
    public Optional<Article> findById(String id) {
        return Optional.ofNullable(articleMapper.findById(id));
    }

    @Override
    @Transactional
    public void save(Article article) {
        if (findById(article.getId()).isPresent()) {
            articleMapper.update(article);
        } else {
            createNewArticle(article);
        }
    }

    private void createNewArticle(Article article) {
        article.getTags()
                .forEach(tag -> {
                    if (!articleMapper.existsTag(tag.getName())) {
                        articleMapper.insertTag(tag);
                    }

                    articleMapper.insertArticleTagRelation(article.getId(), tag.getId());
                });

        articleMapper.insert(article);
    }

    @Override
    public void remove(Article article) {
        articleMapper.delete(article.getId());
    }

}
