package com.spring.boot.example.mybatis.application.profile;

import com.spring.boot.example.mybatis.application.ProfileService;
import com.spring.boot.example.mybatis.application.data.ProfileData;
import com.spring.boot.example.mybatis.core.user.User;
import com.spring.boot.example.mybatis.core.user.UserRepository;
import com.spring.boot.example.mybatis.infrastructure.repository.MyBatisUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@ExtendWith(SpringExtension.class)
@Import({ProfileService.class, MyBatisUserRepository.class})
public class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFetchProfileSuccess() {
        User currentUser = new User("a@test.com", "a", "123", "", "");
        User profileUser = new User("p@test.com", "p", "123", "", "");

        userRepository.save(profileUser);

        Optional<ProfileData> optional = profileService.findByUsername(profileUser.getUsername(), currentUser);

        assertThat(optional).isNotEmpty();
    }

}
