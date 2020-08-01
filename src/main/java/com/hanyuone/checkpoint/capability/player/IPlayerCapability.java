package com.hanyuone.checkpoint.capability.player;

import net.minecraft.util.math.BlockPos;

public interface IPlayerCapability {
    BlockPos getBlockPos();
    void setBlockPos(BlockPos pos);
    void clearBlockPos();

    boolean hasPair();

    int getDistanceWarped();
    void setDistanceWarped(int newDist);
}
