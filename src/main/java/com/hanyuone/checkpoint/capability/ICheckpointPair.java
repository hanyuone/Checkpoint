package com.hanyuone.checkpoint.capability;

import net.minecraft.util.math.BlockPos;

public interface ICheckpointPair {
    public BlockPos getBlockPos();
    public void setBlockPos(BlockPos pos);
    public void clearBlockPos();

    public boolean hasPair();
}
