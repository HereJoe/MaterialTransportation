package com.adl.genius.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DialogOutputVO {
    private DialogVO dialog;
    private String modelOutput;
}
