package com.spring.boot.example.mybatis.infrastructure.mybatis.mapper;

import com.spring.boot.example.mybatis.core.user.FollowRelation;
import com.spring.boot.example.mybatis.core.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findById(@Param("id") String id);

    User findByEmail(@Param("email") String email);

    User findByUsername(@Param("username") String username);

    void insert(@Param("user") User user);

    void update(@Param("user") User user);

    FollowRelation findRelation(@Param("userId") String userId, @Param("targetId") String userTargetId);

    void saveRelation(@Param("followRelation") FollowRelation followRelation);

    void deleteRelation(@Param("followRelation") FollowRelation followRelation);

}
