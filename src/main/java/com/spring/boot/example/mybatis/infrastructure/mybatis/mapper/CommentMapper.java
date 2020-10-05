package com.spring.boot.example.mybatis.infrastructure.mybatis.mapper;

import com.spring.boot.example.mybatis.core.comment.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {

    void insert(@Param("comment") Comment comment);

    Comment findById(@Param("articleId") String articleId, @Param("id") String id);

    void delete(@Param("id") String id);

}
