package com.spring.boot.example.mybatis.application;

import com.spring.boot.example.mybatis.application.data.ArticleData;
import com.spring.boot.example.mybatis.application.data.ArticleDataList;
import com.spring.boot.example.mybatis.application.data.ArticleFavoriteCount;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.ArticleReadService;
import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.UserRelationshipReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleReadService articleReadService;
    private final UserRelationshipReadService userRelationshipReadService;
    private final ArticleFavoritesReadService articleFavoritesReadService;

    public Optional<ArticleData> findBySlug(String slug, User user) {
        ArticleData articleData = articleReadService.findBySlug(slug);

        if (articleData == null) {
            return Optional.empty();
        } else {
            if (user != null) {
                fillExtraInfo(articleData.getId(), user, articleData);
            }

            return Optional.of(articleData);
        }
    }

    public Optional<ArticleData> findById(String id, User user) {
        ArticleData articleData = articleReadService.findById(id);

        if (articleData == null) {
            return Optional.empty();
        } else {
            if (user != null) {
                fillExtraInfo(id, user, articleData);
            }

            return Optional.of(articleData);
        }
    }

    public ArticleDataList findRecentArticles(String tag, String author, String favoriteBy, Page page, User user) {
        List<String> articleIds = articleReadService.queryArticles(tag, author, favoriteBy, page);
        int articleCount = articleReadService.countArticles(tag, author, favoriteBy);

        if (articleIds.isEmpty()) {
            return new ArticleDataList(List.of(), articleCount);
        } else {
            List<ArticleData> articles = articleReadService.findArticles(articleIds);
            fillExtraInfo(articles, user);
            return new ArticleDataList(articles, articleCount);
        }
    }

    public ArticleDataList findUserFeed(User user, Page page) {
        List<String> followedUsers = userRelationshipReadService.followedUsers(user.getId());

        if (followedUsers.isEmpty()) {
            return new ArticleDataList(List.of(), 0);
        } else {
            List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followedUsers, page);
            fillExtraInfo(articles, user);
            int count = articleReadService.countFeedSize(followedUsers);
            return new ArticleDataList(articles, count);
        }
    }

    private void fillExtraInfo(String id, User user, ArticleData articleData) {
        articleData.setFavorited(articleFavoritesReadService.isUserFavorite(user.getId(), id));
        articleData.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(id));
        articleData.getProfileData().setFollowing(
                userRelationshipReadService.isUserFollowing(user.getId(), articleData.getProfileData().getId()));
    }

    private void fillExtraInfo(List<ArticleData> articles, User user) {
        setFavoriteCount(articles);

        if (user != null) {
            setIsFavorite(articles, user);
            setIsFollowingAuthor(articles, user);
        }
    }

    private void setIsFavorite(List<ArticleData> articles, User user) {
        Set<String> favoriteArticles = articleFavoritesReadService.userFavorites(collectArticleIds(articles), user);

        articles.forEach(articleData -> {
            if (favoriteArticles.contains(articleData.getId())) {
                articleData.setFavorited(true);
            }
        });
    }

    private void setIsFollowingAuthor(List<ArticleData> articles, User user) {
        Set<String> followingAuthors = userRelationshipReadService.followingAuthors(user.getId(), collectProfileIds(articles));

        articles.forEach(articleData -> {
            if (followingAuthors.contains(articleData.getProfileData().getId())) {
            articleData.getProfileData().setFollowing(true);
        }
        });
    }

    private void setFavoriteCount(List<ArticleData> articles) {
        List<ArticleFavoriteCount> favoritesCounts = articleFavoritesReadService.articlesFavoriteCount(collectArticleIds(articles));

        Map<String, Integer> countMap = favoritesCounts
                .stream()
                .collect(Collectors.toMap(ArticleFavoriteCount::getId, ArticleFavoriteCount::getCount));

        articles.forEach(articleData -> articleData.setFavoritesCount(countMap.get(articleData.getId())));
    }

    private List<String> collectArticleIds(List<ArticleData> articles) {
        return articles.stream()
                .map(ArticleData::getId).collect(Collectors.toList());
    }

    private List<String> collectProfileIds(List<ArticleData> articles) {
        return articles.stream()
                .map(articleData -> articleData.getProfileData().getId()).collect(toList());
    }

    public boolean canWriteArticle(User user, Article article) {
        return user.getId().equals(article.getUserId());
    }

}
