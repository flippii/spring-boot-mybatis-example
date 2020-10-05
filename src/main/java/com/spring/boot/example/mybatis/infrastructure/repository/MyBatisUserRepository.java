package com.spring.boot.example.mybatis.infrastructure.repository;

import com.spring.boot.example.mybatis.core.user.FollowRelation;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.infrastructure.mybatis.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisUserRepository implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(userMapper.findById(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userMapper.findByEmail(email));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.findByUsername(username));
    }

    @Override
    public void save(User user) {
        if (findById(user.getId()).isPresent()) {
            userMapper.update(user);
        } else {
            userMapper.insert(user);
        }
    }

    @Override
    public Optional<FollowRelation> findRelation(String userId, String userTargetId) {
        return Optional.ofNullable(userMapper.findRelation(userId, userTargetId));
    }

    @Override
    public void saveRelation(FollowRelation followRelation) {
        if (!findRelation(followRelation.getUserId(), followRelation.getTargetId()).isPresent()) {
            userMapper.saveRelation(followRelation);
        }
    }

    @Override
    public void removeRelation(FollowRelation followRelation) {
        userMapper.deleteRelation(followRelation);
    }

}
