package com.spring.boot.example.mybatis.infrastructure.initializer;

import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.comment.Comment;
import com.spring.boot.example.mybatis.core.favorite.ArticleFavorite;
import com.spring.boot.example.mybatis.core.initilizer.DataInitializer;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.ArticleMapper;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.CommentMapper;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Order(2)
@Component
@RequiredArgsConstructor
public class ArticleDataInitializer implements DataInitializer {

    private final ArticleFavoriteMapper articleFavoriteMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public void initialize() {
        User peterMeyer = userMapper.findByEmail("peter.mayer@example.de");
        User paulPanzer = userMapper.findByEmail("paul.panzer@example.com");
        User beateMustermann = userMapper.findByEmail("beate.mustermann@example.com");

        Article javaMagazin = createArticle("Java Magazin", "Das Java Magazin begleitet seit über 20 Jahren alle wesentlichen " +
                        "Entwicklungen der Java-Welt aktuell, kritisch und mit großer praktischer Relevanz.", "",
                         new String[] {"Java", "Programmierung"}, paulPanzer);

        Article entwicklerMagazin = createArticle("Entwickler Magazin", "Das Entwickler Magazin bietet Software-Entwicklern Einblicke " +
                        "und Orientierung in einem Markt an Entwicklungstechnologien, -tools und -ansätzen, der stets ...",
                    "", new String[] {"Java", "Programmierung", "PHP"}, peterMeyer);

        Article phpMagazin = createArticle("PHP Magazin", "Das PHP Magazin liefert die gesamte Bandbreite an Wissen, das für moderne " +
                        "Webanwendungen benötigt wird – von PHP-Programmierthemen über JavaScript ...", "",
                        new String[] {"PHP", "Programmierung"}, beateMustermann);

        Article javascriptMagazin = createArticle("Javascript Magazin", "Das JavaScript Magazine: Neu & kostenlos zum Download! " +
                        "Ausgabe 1 befasst sich mit Angular: Features im Überblick, Ivy und Code Smells.", "",
                        new String[] {"Javascript", "Programmierung"}, beateMustermann);

        Stream.of(javaMagazin, entwicklerMagazin, phpMagazin, javascriptMagazin)
                .filter(article ->
                        Optional.ofNullable(articleMapper.findById(article.getId())).isEmpty()
                )
                .forEach(articleMapper::insert);

        List<ArticleFavorite> articleFavorites = List.of(
                new ArticleFavorite(javaMagazin.getId(), peterMeyer.getId()),
                new ArticleFavorite(javascriptMagazin.getId(), paulPanzer.getId()),
                new ArticleFavorite(entwicklerMagazin.getId(), beateMustermann.getId()),
                new ArticleFavorite(phpMagazin.getId(), beateMustermann.getId()),
                new ArticleFavorite(javascriptMagazin.getId(), paulPanzer.getId())
        );

        articleFavorites.stream()
                .filter(articleFavorite ->
                        Optional.ofNullable(articleFavoriteMapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId())).isEmpty()
                )
                .forEach(articleFavoriteMapper::insert);

        List<Comment> comments = List.of(
                createComment("Zu empfehlen.", paulPanzer, javaMagazin),
                createComment("Zu empfehlen.", paulPanzer, javascriptMagazin),
                createComment("Zu empfehlen.", beateMustermann, entwicklerMagazin),
                createComment("Zu empfehlen.", beateMustermann, phpMagazin),
                createComment("Zu empfehlen.", peterMeyer, javaMagazin)
        );

        comments.stream()
                .filter(comment ->
                        Optional.ofNullable(commentMapper.findById(comment.getArticleId(), comment.getId())).isEmpty()
                )
                .forEach(commentMapper::insert);
    }

    private Article createArticle(String title, String description, String body, String[] tagList, User user) {
        return new Article(title, description, body, tagList, user.getId());
    }

    private Comment createComment(String body, User user, Article article) {
        return new Comment(body, user.getId(), article.getId());
    }

}
