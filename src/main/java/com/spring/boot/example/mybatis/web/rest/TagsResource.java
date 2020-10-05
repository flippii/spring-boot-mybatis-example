package com.spring.boot.example.mybatis.web.rest;

import com.spring.boot.example.mybatis.application.TagsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/tags")
public class TagsResource {

    private final TagsService tagsService;

    @GetMapping
    public ResponseEntity<?> tags() {
        return ResponseEntity.ok(Map.of("tags", tagsService.allTags()));
    }

}
