package com.spring.boot.example.mybatis.core.user;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    void save(User user);

    Optional<FollowRelation> findRelation(String userId, String userTargetId);

    void saveRelation(FollowRelation followRelation);

    void removeRelation(FollowRelation followRelation);

}
