package com.spring.boot.example.mybatis.infrastructure.mybatis.readservice;

import com.spring.boot.example.mybatis.application.Page;
import com.spring.boot.example.mybatis.application.data.ArticleData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleReadService {

    ArticleData findBySlug(@Param("slug") String slug);

    ArticleData findById(@Param("id") String id);

    List<String> queryArticles(@Param("tag") String tag,
                               @Param("author") String author,
                               @Param("favoriteBy") String favoriteBy,
                               @Param("page") Page page);

    int countArticles(@Param("tag") String tag,
                      @Param("author") String author,
                      @Param("favoriteBy") String favoriteBy);

    List<ArticleData> findArticles(@Param("articleIds") List<String> articleIds);

    List<ArticleData> findArticlesOfAuthors(@Param("authors") List<String> authors, @Param("page") Page page);

    int countFeedSize(@Param("authors") List<String> authors);

}
