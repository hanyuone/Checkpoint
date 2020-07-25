package com.hanyuone.checkpoint.tileentity;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairHandler;
import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.register.TileEntityRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
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
            protected void onContentsChanged(int slot) {
                markDirty();
            }

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

    // NBT stuff

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

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return super.getUpdatePacket();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {

    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = new CompoundNBT();
        return this.write(tag);
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        this.read(tag);
    }

    // Capabilities

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

    // Programmatically get/spend pearls

    public int calculateCost() {
        if (this.pairHandler.hasPair()) {
            double distance = Math.sqrt(this.pos.distanceSq(this.pairHandler.getBlockPos()));
            return (int) Math.ceil(distance / 100);
        } else {
            return -1;
        }
    }

    public int getEnderPearls() {
        return this.pearlHandler.getStackInSlot(0).getCount();
    }

    public void spendEnderPearls() {
        this.pearlHandler.extractItem(0, this.calculateCost(), false);
        this.markDirty();
    }
}
