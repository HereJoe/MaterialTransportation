package com.adl.genius.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {
    private Integer id;
    private String username;

    public String getRedisKey() {
        return "token:" + id;
    }
}
