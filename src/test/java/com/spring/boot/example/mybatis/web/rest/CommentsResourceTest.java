package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.CommentService;
import com.spring.boot.example.mybatis.application.data.CommentData;
import com.spring.boot.example.mybatis.application.data.ProfileData;
import com.spring.boot.example.mybatis.configuration.JacksonConfiguration;
import com.spring.boot.example.mybatis.configuration.SecurityConfiguration;
import com.spring.boot.example.mybatis.core.article.Article;
import com.spring.boot.example.mybatis.core.article.ArticleRepository;
import com.spring.boot.example.mybatis.core.comment.Comment;
import com.spring.boot.example.mybatis.core.comment.CommentRepository;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.infrastructure.security.DomainUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.spring.boot.example.mybatis.utils.MvcContentMapper.writeValueAsBytes;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentsResource.class)
@Import({SecurityConfiguration.class, DomainUserDetailsService.class, JacksonConfiguration.class, TestConfiguration.class})
public class CommentsResourceTest extends TestWithCurrentUser {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private CommentService commentService;

    private Article article;
    private CommentData commentData;
    private Comment comment;

    @BeforeEach
    void before() {
        super.before();

        article = new Article(
                "title",
                "desc",
                "body",
                new String[] {"test", "java"},
                currentUser.getId());

        given(articleRepository.findBySlug(eq(article.getSlug()))).willReturn(Optional.of(article));

        comment = new Comment("comment text", currentUser.getId(), article.getId());

        commentData = CommentData.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .articleId(comment.getArticleId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getCreatedAt())
                .profileData(ProfileData.builder()
                        .id(currentUser.getId())
                        .username(currentUser.getUsername())
                        .bio(currentUser.getBio())
                        .image(currentUser.getImage())
                        .following(false)
                        .build())
                .build();
    }

    @Test
    void testCreateCommentSuccess() throws Exception {
        Map<String, Object> createParam = prepareParam("comment text");

        given(commentService.findById(anyString(), eq(currentUser))).willReturn(Optional.of(commentData));

        mockMvc.perform(post("/api/articles/" + article.getSlug() + "/comments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(writeValueAsBytes(createParam)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment.body").value(commentData.getBody()));

        verify(commentRepository).save(any());
    }

    @Test
    void testGet422WithEmptyBody() throws Exception {
        Map<String, Object> createParam = prepareParam("");

        mockMvc.perform(post("/api/articles/" + article.getSlug() + "/comments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsBytes(createParam)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.body[0]").value("can't be empty"));
    }

    @Test
    void testGetCommentsOfArticleSuccess() throws Exception {
        given(commentService.findByArticleId(eq(article.getId()), eq(currentUser))).willReturn(List.of(commentData));

        mockMvc.perform(get("/api/articles/" + article.getSlug() + "/comments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[0].id").value(commentData.getId()));
    }

    @Test
    void testDeleteCommentSuccess() throws Exception {
        given(commentRepository.findById(eq(article.getId()), eq(comment.getId()))).willReturn(Optional.of(comment));
        given(commentService.canWriteComment(eq(currentUser), eq(article), eq(comment))).willReturn(true);

        mockMvc.perform(delete("/api/articles/" + article.getSlug() + "/comments/" + comment.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGet403IfNotAuthorOfArticleOrAuthorOfCommentWhenDeleteComment() throws Exception {
        User anotherUser = new User("other@example.com","other","123","","");

        given(userRepository.findByUsername(eq(anotherUser.getUsername()))).willReturn(Optional.of(anotherUser));
        given(userRepository.findById(eq(anotherUser.getId()))).willReturn(Optional.of(anotherUser));

        given(commentRepository.findById(eq(article.getId()), eq(comment.getId()))).willReturn(Optional.of(comment));

        mockMvc.perform(delete("/api/articles/" + article.getSlug() + "/comments/" + comment.getId())
                .header("Authorization", "Bearer " + testUserService.createToken(anotherUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

    }

    private Map<String, Object> prepareParam(String body) {
        return Map.of("comment", Map.of("body", body));
    }

}
