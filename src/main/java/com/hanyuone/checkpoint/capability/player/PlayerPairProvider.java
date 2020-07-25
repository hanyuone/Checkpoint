package com.hanyuone.checkpoint.capability.player;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerPairProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(IPlayerPair.class)
    public static final Capability<IPlayerPair> PLAYER_PAIR = null;

    private LazyOptional<IPlayerPair> instance = LazyOptional.of(PLAYER_PAIR::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_PAIR ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return PLAYER_PAIR.getStorage().writeNBT(PLAYER_PAIR, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        PLAYER_PAIR.getStorage().readNBT(PLAYER_PAIR, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null, nbt);
    }
}
