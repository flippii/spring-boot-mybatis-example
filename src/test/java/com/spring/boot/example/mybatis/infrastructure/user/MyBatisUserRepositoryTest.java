package com.spring.boot.example.mybatis.infrastructure.user;

import com.spring.boot.example.mybatis.core.user.FollowRelation;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

@MybatisTest
@ExtendWith(SpringExtension.class)
@Import({MyBatisUserRepository.class})
public class MyBatisUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void before() {
        user = createUserStub();
    }

    @Test
    void testSaveAndFetchUserSuccess() {
        userRepository.save(user);

        Optional<User> optionalUserByUsername = userRepository.findByUsername(user.getUsername());
        assertThat(optionalUserByUsername).isNotEmpty();
        assertThat(optionalUserByUsername.get()).isEqualToComparingFieldByField(user);

        Optional<User> optionalUserByEmail = userRepository.findByEmail(user.getEmail());
        assertThat(optionalUserByEmail).isNotEmpty();
        assertThat(optionalUserByEmail.get()).isEqualToComparingFieldByField(user);
    }

    @Test
    void testUpdateUserSuccess() {
        String newUsername = "newUsername";
        String newEmail  = "newemail@email.com";

        userRepository.save(user);

        user.update(newEmail, newUsername, "", "", "");
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findById(user.getId());

        assertThat(optionalUser).isNotEmpty();
        assertThat(optionalUser.get().getEmail()).isEqualTo(newEmail);
        assertThat(optionalUser.get().getUsername()).isEqualTo(newUsername);
    }

    @Test
    void testCreateNewUserFollowSuccess() {
        User other = new User("other@example.com", "other", "123", "", "");
        userRepository.save(other);

        FollowRelation followRelation = new FollowRelation(user.getId(), other.getId());
        userRepository.saveRelation(followRelation);

        Optional<FollowRelation> optionalUser = userRepository.findRelation(user.getId(), other.getId());

        assertThat(optionalUser).isNotEmpty();
        assertThat(optionalUser.get()).isEqualToComparingFieldByField(followRelation);
    }

    @Test
    void testUnfollowUserSuccess() {
        User other = new User("other@example.com", "other", "123", "", "");
        userRepository.save(other);

        FollowRelation followRelation = new FollowRelation(user.getId(), other.getId());
        userRepository.saveRelation(followRelation);

        userRepository.removeRelation(followRelation);

        assertThat(userRepository.findRelation(user.getId(), other.getId())).isEmpty();
    }

    private User createUserStub() {
        return new User("aisensiy@163.com", "aisensiy", "123", "", "default");
    }

}
