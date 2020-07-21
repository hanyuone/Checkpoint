package com.hanyuone.checkpoint.capability;

import net.minecraft.util.math.BlockPos;

public interface ICheckpointPair {
    BlockPos getBlockPos();
    void setBlockPos(BlockPos pos);
    void clearBlockPos();

    boolean hasPair();
}
