package com.adl.path.bean;

import lombok.Data;

import java.util.LinkedList;

@Data
public class Combine implements Cloneable {
    private int batchId;
    private int totalCost;
    private LinkedList<Path> paths = new LinkedList<>();

    @Override
    public Combine clone() {
        Combine combine;
        try {
            combine = (Combine) super.clone();
            combine.setPaths(new LinkedList<>(this.paths));
            // needn't clone each Path
            return combine;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
