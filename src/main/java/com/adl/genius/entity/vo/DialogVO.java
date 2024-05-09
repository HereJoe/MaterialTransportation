package com.adl.genius.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DialogVO {
    private Integer id;
    private String title;
    private LocalDateTime lastActiveTime;
}
