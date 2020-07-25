package com.hanyuone.checkpoint.capability.player;

import net.minecraft.util.math.BlockPos;

public interface IPlayerPair {
    BlockPos getBlockPos();
    void setBlockPos(BlockPos pos);
    void clearBlockPos();

    boolean hasPair();
}
