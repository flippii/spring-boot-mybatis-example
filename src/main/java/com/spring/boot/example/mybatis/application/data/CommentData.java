package com.spring.boot.example.mybatis.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentData {

    private String id;
    private String body;
    @JsonIgnore
    private String articleId;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    @JsonProperty("author")
    private ProfileData profileData;
    
}
