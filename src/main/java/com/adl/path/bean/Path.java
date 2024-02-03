package com.adl.path.bean;

import lombok.Data;

import java.util.BitSet;

@Data
public class Path extends BasePath implements Cloneable {
    private int batchId;
    private int id;
    private String[] nodeNames;
    private int[] nodeCosts;
    private int[] edgeCosts;
    private BitSet sharedNodeBit;
    private BitSet sharedEdgeBit;
    private int sharedCost;
    private boolean formatted;

    @Override
    public Path clone() {
        try {
        Path path = (Path) super.clone();
        path.setSharedNodeBit(new BitSet(sharedNodeBit.length()));
        path.setSharedEdgeBit(new BitSet(sharedEdgeBit.length()));
        return path;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
