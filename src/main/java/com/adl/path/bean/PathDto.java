package com.adl.path.bean;

import lombok.Data;

@Data
public class PathDto {
    private int batchId;
    private int source;
    private int target;
    private int totalNode;
    private int totalCost;
    private String path;
    private String createdBy;

}
