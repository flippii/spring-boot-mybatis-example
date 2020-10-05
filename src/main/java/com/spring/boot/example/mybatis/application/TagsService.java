package com.spring.boot.example.mybatis.application;

import com.spring.boot.example.mybatis.infrastructure.mybatis.readservice.TagReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagsService {

    private final TagReadService tagReadService;

    public List<String> allTags() {
        return tagReadService.findAll();
    }

}
