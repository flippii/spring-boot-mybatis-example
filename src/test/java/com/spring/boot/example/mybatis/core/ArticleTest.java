package com.spring.boot.example.mybatis.core;

import com.spring.boot.example.mybatis.core.article.Article;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleTest {

    @Test
    public void testGetRightSlug() {
        Article article = new Article("a new   title", "desc", "body", new String[]{"java"}, "123");

        assertThat(article.getSlug()).isEqualTo("a-new-title");
    }

    @Test
    public void testGetRightSlugWithNumberInTitle() {
        Article article = new Article("a new title 2", "desc", "body", new String[]{"java"}, "123");

        assertThat(article.getSlug()).isEqualTo("a-new-title-2");
    }

    @Test
    public void testGetLowerCaseSlug() {
        Article article = new Article("A NEW TITLE", "desc", "body", new String[]{"java"}, "123");

        assertThat(article.getSlug()).isEqualTo("a-new-title");
    }

    @Test
    public void testHandleOtherLanguage() {
        Article article = new Article("中文：标题", "desc", "body", new String[]{"java"}, "123");

        assertThat(article.getSlug()).isEqualTo("中文-标题");
    }

    @Test
    public void testHandleCommas() {
        Article article = new Article("what?the.hell,w", "desc", "body", new String[]{"java"}, "123");

        assertThat(article.getSlug()).isEqualTo("what-the-hell-w");
    }

}
