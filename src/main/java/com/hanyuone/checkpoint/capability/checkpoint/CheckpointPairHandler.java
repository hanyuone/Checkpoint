package com.hanyuone.checkpoint.capability.checkpoint;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class CheckpointPairHandler implements ICheckpointPair, INBTSerializable<CompoundNBT> {
    private BlockPos pos;
    private boolean hasPair;
    private UUID playerId;

    public CheckpointPairHandler() {
        this.pos = BlockPos.ZERO;
        this.hasPair = false;
        this.playerId = null;
    }

    @Override
    public BlockPos getBlockPos() {
        return this.pos;
    }

    @Override
    public void setBlockPos(BlockPos pos) {
        this.pos = pos;
        this.hasPair = true;
    }

    @Override
    public void clearBlockPos() {
        this.pos = BlockPos.ZERO;
        this.hasPair = false;
    }

    @Override
    public UUID getPlayerId() {
        return this.playerId;
    }

    @Override
    public void setPlayerId(UUID id) {
        this.playerId = id;
    }

    @Override
    public boolean hasPair() {
        return this.hasPair;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong("block_pos", this.getBlockPos().toLong());
        tag.putBoolean("has_pair", this.hasPair());
        tag.putUniqueId("player_id", this.playerId);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag) {
        if (tag.getBoolean("has_pair")) {
            this.setBlockPos(BlockPos.fromLong(tag.getLong("block_pos")));
        }

        this.setPlayerId(tag.getUniqueId("player_id"));
    }
}
