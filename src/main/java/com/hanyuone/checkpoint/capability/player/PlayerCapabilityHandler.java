package com.hanyuone.checkpoint.capability.player;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerCapabilityHandler implements IPlayerCapability, INBTSerializable<CompoundNBT> {
    private BlockPos pos;
    private boolean hasPair;

    private int distanceWarped;

    public PlayerCapabilityHandler() {
        this.pos = BlockPos.ZERO;
        this.hasPair = false;

        this.distanceWarped = 0;
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
    public boolean hasPair() {
        return this.hasPair;
    }

    @Override
    public int getDistanceWarped() {
        return this.distanceWarped;
    }

    @Override
    public void setDistanceWarped(int newDist) {
        this.distanceWarped = newDist;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong("block_pos", this.getBlockPos().toLong());
        tag.putBoolean("has_pair", this.hasPair());

        tag.putInt("distance_warped", this.distanceWarped);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag) {
        if (tag.getBoolean("has_pair")) {
            this.setBlockPos(BlockPos.fromLong(tag.getLong("block_pos")));
        }

        this.setDistanceWarped(tag.getInt("distance_warped"));
    }
}
