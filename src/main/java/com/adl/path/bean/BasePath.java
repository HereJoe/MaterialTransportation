package com.adl.path.bean;

import lombok.Data;

@Data
public class BasePath {
    private String pathStr;
    private String formatPathStr;
    private String sharedStr;
    private int pathCost;
    private int[] nodeIds;
    private int[] edgeIds;

}
