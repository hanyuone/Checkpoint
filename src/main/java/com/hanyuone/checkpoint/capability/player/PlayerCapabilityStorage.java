package com.hanyuone.checkpoint.capability.player;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class PlayerCapabilityStorage implements Capability.IStorage<IPlayerCapability> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<IPlayerCapability> capability, IPlayerCapability instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong("block_pos", instance.getBlockPos().toLong());
        tag.putBoolean("has_pair", instance.hasPair());

        tag.putInt("distance_warped", instance.getDistanceWarped());
        return tag;
    }

    @Override
    public void readNBT(Capability<IPlayerCapability> capability, IPlayerCapability instance, Direction side, INBT nbt) {
        CompoundNBT tag = (CompoundNBT) nbt;

        if (tag.getBoolean("has_pair")) {
            instance.setBlockPos(BlockPos.fromLong(tag.getLong("block_pos")));
        }

        instance.setDistanceWarped(tag.getInt("distance_warped"));
    }
}
