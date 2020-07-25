package com.hanyuone.checkpoint.capability.checkpoint;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class CheckpointPairStorage implements Capability.IStorage<ICheckpointPair> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<ICheckpointPair> capability, ICheckpointPair instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong("block_pos", instance.getBlockPos().toLong());
        tag.putBoolean("has_pair", instance.hasPair());
        tag.putUniqueId("player_id", instance.getPlayerId());
        return tag;
    }

    @Override
    public void readNBT(Capability<ICheckpointPair> capability, ICheckpointPair instance, Direction side, INBT nbt) {
        CompoundNBT tag = (CompoundNBT) nbt;

        if (tag.getBoolean("has_pair")) {
            instance.setBlockPos(BlockPos.fromLong(tag.getLong("block_pos")));
        }

        instance.setPlayerId(tag.getUniqueId("player_id"));
    }
}
