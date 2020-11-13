package com.hanyuone.checkpoint.tileentity;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairHandler;
import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.capability.checkpoint.ICheckpointPair;
import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import com.hanyuone.checkpoint.network.CheckpointPacketHandler;
import com.hanyuone.checkpoint.network.SyncPlayerPacket;
import com.hanyuone.checkpoint.register.TileEntityRegister;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

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
        CompoundNBT nbtTagCompound = new CompoundNBT();
        this.write(nbtTagCompound);
        // Arbitrary number, only used for vanilla TileEntities
        int tileEntityType = 42;
        return new SUpdateTileEntityPacket(this.pos, tileEntityType, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(pkt.getNbtCompound());
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
            return (int) Math.ceil(distance / 100) - this.pairHandler.getChargingPearls();
        } else {
            return -1;
        }
    }

    // Get the number of ender pearls stored in the ItemStackHandler capability
    public int getEnderPearls() {
        return this.pearlHandler.getStackInSlot(0).getCount();
    }

    // Spend ender pearls:
    // - if the amount of ender pearls stored in the checkpoint aren't enough
    //   to cover the cost of teleportation, store them up in the checkpoint itself
    //   as "charging pearls"
    // - if it *is* enough (+ charging pearls), spend it all and reset the amount
    //   of charging pearls to 0
    public void spendEnderPearls() {
        int availablePearls = this.getEnderPearls();
        int cost = this.calculateCost();

        if (availablePearls < cost) {
            this.pearlHandler.extractItem(0, availablePearls, false);
            this.pairHandler.setChargingPearls(availablePearls + this.pairHandler.getChargingPearls());
        } else {
            this.pearlHandler.extractItem(0, cost, false);
            this.pairHandler.setChargingPearls(0);
        }

        this.markDirty();
    }

    // Disable the other half of the checkpoint
    public void disablePair(World worldIn, BlockPos pos) {
        if (this.pairHandler.hasPair()) {
            TileEntity otherEntity = worldIn.getTileEntity(this.pairHandler.getBlockPos());

            if (otherEntity instanceof CheckpointTileEntity) {
                otherEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
                    handler.clearBlockPos();
                    handler.clearPlayerId();
                });
                otherEntity.markDirty();
            }
        }

        if (!this.pairHandler.isIdEmpty()) {
            PlayerEntity playerFromEntity = worldIn.getPlayerByUuid(this.pairHandler.getPlayerId());

            // If the checkpoint was just made, delete the saved data on the player
            // so the next checkpoint doesn't point to a dead BlockPos
            playerFromEntity.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(playerHandler -> {
                if (playerHandler.hasPair() && playerHandler.getBlockPos().equals(pos)) {
                    playerHandler.clearBlockPos();
                    SyncPlayerPacket packet = new SyncPlayerPacket(false, BlockPos.ZERO, playerHandler.getDistanceWarped());
                    CheckpointPacketHandler.INSTANCE.sendToServer(packet);
                }
            });

            this.pairHandler.clearPlayerId();
        }
    }

    // Finds a suitable block to spawn on (should be server-side)
    @Nullable
    public BlockPos suitablePos() {
        if (!this.pairHandler.hasPair()) {
            return null;
        }

        BlockPos pairPos = this.pairHandler.getBlockPos();
        BlockPos[] neighbours = {pairPos.north(), pairPos.east(), pairPos.south(), pairPos.west()};

        AbstractChunkProvider chunkProvider = this.world.getChunkProvider();
        ServerWorld serverWorld = this.world.getServer().getWorld(DimensionType.OVERWORLD);

        for (BlockPos neighbour: neighbours) {
            ChunkPos chunkPos = new ChunkPos(neighbour);
            boolean needToLoad = !chunkProvider.isChunkLoaded(chunkPos);

            if (needToLoad) {
                serverWorld.forceChunk(chunkPos.x, chunkPos.z, true);
            }

            BlockState currentState = this.world.getBlockState(neighbour);
            BlockState belowState = this.world.getBlockState(neighbour.down());
            BlockState aboveState = this.world.getBlockState(neighbour.up());

            if (needToLoad) {
                serverWorld.forceChunk(chunkPos.x, chunkPos.z, false);
            }

            if (aboveState.isAir() && currentState.isAir() && belowState.isSolid()) {
                return neighbour;
            }
        }

        return null;
    }
}
