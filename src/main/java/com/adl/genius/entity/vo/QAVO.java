package com.adl.genius.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QAVO {
    private String userInput;
    private String modelOutput;
    private LocalDateTime createTime;
}
