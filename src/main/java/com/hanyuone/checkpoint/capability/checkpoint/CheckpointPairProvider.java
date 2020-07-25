package com.hanyuone.checkpoint.capability.checkpoint;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CheckpointPairProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(ICheckpointPair.class)
    public static final Capability<ICheckpointPair> CHECKPOINT_PAIR = null;

    private LazyOptional<ICheckpointPair> instance = LazyOptional.of(CHECKPOINT_PAIR::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CHECKPOINT_PAIR ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return CHECKPOINT_PAIR.getStorage().writeNBT(CHECKPOINT_PAIR, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CHECKPOINT_PAIR.getStorage().readNBT(CHECKPOINT_PAIR, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null, nbt);
    }
}
