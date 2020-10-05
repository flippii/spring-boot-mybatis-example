package com.spring.boot.example.mybatis.infrastructure.mybatis.readservice;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagReadService {

    List<String> findAll();

}
