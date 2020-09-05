package com.hanyuone.checkpoint.tileentity;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.register.TileEntityRegister;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpperTileEntity extends TileEntity implements ITickableTileEntity {
    public UpperTileEntity() {
        super(TileEntityRegister.UPPER_TILE_ENTITY.get());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) || cap.equals(CheckpointPairProvider.CHECKPOINT_PAIR)) {
            BlockPos lower = this.pos.down();
            TileEntity entity = this.world.getTileEntity(lower);

            if (entity instanceof CheckpointTileEntity) {
                return entity.getCapability(cap, side);
            }
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void tick() {

    }
}
