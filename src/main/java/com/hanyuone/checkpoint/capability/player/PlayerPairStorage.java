package com.hanyuone.checkpoint.capability.player;

import com.hanyuone.checkpoint.capability.checkpoint.ICheckpointPair;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class PlayerPairStorage implements Capability.IStorage<IPlayerPair> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<IPlayerPair> capability, IPlayerPair instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong("block_pos", instance.getBlockPos().toLong());
        tag.putBoolean("has_pair", instance.hasPair());
        return tag;
    }

    @Override
    public void readNBT(Capability<IPlayerPair> capability, IPlayerPair instance, Direction side, INBT nbt) {
        CompoundNBT tag = (CompoundNBT) nbt;

        if (tag.getBoolean("has_pair")) {
            instance.setBlockPos(BlockPos.fromLong(tag.getLong("block_pos")));
        }
    }
}
