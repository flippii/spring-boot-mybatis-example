package com.spring.boot.example.mybatis.infrastructure.mybatis.readservice;

import com.spring.boot.example.mybatis.application.data.UserData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserReadService {

    UserData findByUsername(@Param("username") String username);

    UserData findById(@Param("id") String id);

    UserData findByEmail(@Param("email") String email);

}
