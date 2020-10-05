package com.spring.boot.example.mybatis.infrastructure.mybatis.mapper;

import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleMapper {

    Article findBySlug(@Param("slug") String slug);

    Article findById(@Param("id") String id);

    void insert(@Param("article") Article article);

    void update(@Param("article")  Article article);

    void delete(@Param("id") String id);

    boolean existsTag(@Param("tagName") String name);

    void insertTag(@Param("tag") Tag tag);

    void insertArticleTagRelation(@Param("articleId") String articleId, @Param("tagId") String tagId);

}
