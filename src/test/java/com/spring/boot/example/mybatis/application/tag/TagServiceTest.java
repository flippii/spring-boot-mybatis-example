package com.spring.boot.example.mybatis.application.tag;

import com.spring.boot.example.mybatis.application.TagsService;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@ExtendWith(SpringExtension.class)
@Import({TagsService.class, MyBatisArticleRepository.class})
public class TagServiceTest {

    @Autowired
    private TagsService tagsService;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void testGetAllTags() {
        articleRepository.save(new Article("test", "test", "test", new String[] {"java"}, "123"));

        assertThat(tagsService.allTags()).contains("java");
    }

}
