package com.spring.boot.example.mybatis.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileData {

    @JsonIgnore
    private String id;
    private String username;
    private String bio;
    private String image;
    private boolean following;

}
