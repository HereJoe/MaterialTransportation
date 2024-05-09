package com.adl.genius.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DialogInputVO {
    private String user;
    private Integer dialogId;
    private String userInput;
}
