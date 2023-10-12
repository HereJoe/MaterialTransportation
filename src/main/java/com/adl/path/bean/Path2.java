package com.adl.path.bean;

import lombok.Data;

import java.util.BitSet;

@Data
public class Path2 extends BasePath2 {
    private int batchId;
    private int id;
    private String[] nodeNames;
    private int[] nodeCosts;
    private int[] edgeCosts;
    private BitSet sharedNodeBit;
    private BitSet sharedEdgeBit;
    private int sharedCost;
    private boolean formatted;

    public Path2(int batchId, int[] nodeIds, String[] nodeNames, int pathCost, int[] nodeCosts, int[] edgeCosts) {
        super(null,pathCost,nodeIds);
        this.batchId = batchId;
        this.nodeNames = nodeNames;
        this.nodeCosts = nodeCosts;
        this.edgeCosts = edgeCosts;
    }

    public Path2() {

    }
}
