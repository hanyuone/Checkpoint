package com.hanyuone.checkpoint.tileentity;

import com.hanyuone.checkpoint.capability.CheckpointPairHandler;
import com.hanyuone.checkpoint.capability.CheckpointPairProvider;
import com.hanyuone.checkpoint.register.TileEntityRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CheckpointTileEntity extends TileEntity {
    private final ItemStackHandler pearlHandler = this.createPearlHandler();
    private final LazyOptional<ItemStackHandler> lazyPearlHandler = LazyOptional.of(() -> pearlHandler);

    private final CheckpointPairHandler pairHandler = new CheckpointPairHandler();
    private final LazyOptional<CheckpointPairHandler> lazyPairHandler = LazyOptional.of(() -> pairHandler);

    private ItemStackHandler createPearlHandler() {
        return new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == Items.ENDER_PEARL;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (stack.getItem() != Items.ENDER_PEARL) {
                    return stack;
                }

                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    public CheckpointTileEntity() {
        super(TileEntityRegister.CHECKPOINT_TILE_ENTITY.get());
    }

    @Override
    public void read(CompoundNBT compound) {
        this.pearlHandler.deserializeNBT(compound.getCompound("pearls"));
        this.pairHandler.deserializeNBT(compound.getCompound("pair"));

        super.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put("pearls", this.pearlHandler.serializeNBT());
        compound.put("pair", this.pairHandler.serializeNBT());

        return super.write(compound);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) {
            return this.lazyPearlHandler.cast();
        } else if (cap.equals(CheckpointPairProvider.CHECKPOINT_PAIR)) {
            return this.lazyPairHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    public int calculateCost() {
        if (this.pairHandler.hasPair()) {
            double distance = Math.sqrt(this.pos.distanceSq(this.pairHandler.getBlockPos()));
            return (int) Math.ceil(distance / 100);
        } else {
            return -1;
        }
    }
}
