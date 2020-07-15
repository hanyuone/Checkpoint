package com.hanyuone.checkpoint.block;

import net.minecraft.util.IStringSerializable;

public enum CheckpointHalf implements IStringSerializable {
    UPPER, LOWER;

    public String toString() {
        return this.getName();
    }

    public String getName() {
        // Names must be in lowercase!
        return this == UPPER ? "upper" : "lower";
    }
}
