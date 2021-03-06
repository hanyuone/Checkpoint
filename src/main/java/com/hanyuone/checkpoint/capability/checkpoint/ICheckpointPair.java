package com.hanyuone.checkpoint.capability.checkpoint;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface ICheckpointPair {
    BlockPos getBlockPos();
    void setBlockPos(BlockPos pos);
    void clearBlockPos();

    UUID getPlayerId();
    void setPlayerId(UUID id);
    void clearPlayerId();

    int getChargingPearls();
    void setChargingPearls(int pearls);

    boolean hasPair();
    boolean isIdEmpty();
}
