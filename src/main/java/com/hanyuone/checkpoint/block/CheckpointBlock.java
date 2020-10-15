package com.hanyuone.checkpoint.block;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import com.hanyuone.checkpoint.container.CheckpointContainer;
import com.hanyuone.checkpoint.item.PairerItem;
import com.hanyuone.checkpoint.network.CheckpointPacketHandler;
import com.hanyuone.checkpoint.network.SyncPlayerPacket;
import com.hanyuone.checkpoint.register.BlockRegister;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CheckpointBlock extends Block {
    private static final VoxelShape LOWER = VoxelShapes.or(
            makeCuboidShape(1, 0, 1, 15, 2, 15),
            makeCuboidShape(2, 2, 2, 14, 3, 14),
            makeCuboidShape(4, 3, 4, 12, 15, 12)
    );

    public CheckpointBlock() {
        super(Block.Properties.create(Material.ROCK)
                .hardnessAndResistance(1.5f, 6)
                .sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CheckpointTileEntity();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return LOWER;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, Blocks.AIR.getDefaultState(), te, stack);
    }

    @Override
    public void onBlockHarvested(World worldIn, @Nonnull BlockPos pos, BlockState state, @Nonnull PlayerEntity player) {
        BlockPos upperPos = pos.up();
        BlockState upperState = worldIn.getBlockState(upperPos);

        TileEntity checkpointEntity = worldIn.getTileEntity(pos);

        if (checkpointEntity instanceof CheckpointTileEntity) {
            ((CheckpointTileEntity) checkpointEntity).disablePair(worldIn, pos);
        }

        // Checks if the other half of the checkpoint we want to destroy is
        // *actually* the other half
        if (upperState.getBlock() instanceof UpperBlock) {
            worldIn.setBlockState(upperPos, Blocks.AIR.getDefaultState(), 35);
            worldIn.playEvent(player, 2001, upperPos, Block.getStateId(upperState));
            ItemStack itemStack = player.getHeldItemMainhand();

            if (!worldIn.isRemote && !player.isCreative() && player.canHarvestBlock(upperState) && checkpointEntity instanceof CheckpointTileEntity) {
                Block.spawnDrops(state, worldIn, pos, null, player, itemStack);
                Block.spawnDrops(upperState, worldIn, upperPos, null, player, itemStack);

                ItemStack enderPearls = new ItemStack(Items.ENDER_PEARL, ((CheckpointTileEntity) checkpointEntity).getEnderPearls());
                worldIn.addEntity(new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), enderPearls));
            }
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    // Triggers when a checkpoint is placed down or when a pairer interacts with
    // an existing checkpoint - updates the player's pairing according to the following
    // rule:
    // - if the player has not established a pair yet (i.e. they have not placed down
    //   their first checkpoint), that player's capability is updated with the position
    //   of the first checkpoint
    // - if the player already placed down half of the pair, that player's checkpoint position
    //   is cleared and the first checkpoint is updated
    private void setPlayerPair(World worldIn, BlockPos pos, @Nonnull LivingEntity placer, TileEntity tileEntity) {
        placer.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(placerHandler -> {
            // If the player already has an existing half stored as a capability:
            if (placerHandler.hasPair() && !placerHandler.getBlockPos().equals(pos)) {
                BlockPos oldPos = placerHandler.getBlockPos();
                TileEntity oldEntity = worldIn.getTileEntity(oldPos);

                // Link both halves of the checkpoint pair together
                // (also notify the world of changes so client will render GUI properly)
                if (tileEntity instanceof CheckpointTileEntity && oldEntity instanceof CheckpointTileEntity) {
                    tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(entityHandler -> entityHandler.setBlockPos(oldPos));
                    tileEntity.markDirty();
                    worldIn.notifyBlockUpdate(pos, worldIn.getBlockState(pos), worldIn.getBlockState(pos), 3);

                    oldEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(oldHandler -> oldHandler.setBlockPos(pos));
                    oldEntity.markDirty();
                    worldIn.notifyBlockUpdate(oldPos, worldIn.getBlockState(oldPos), worldIn.getBlockState(oldPos), 3);
                }

                // Clear the player capability
                placerHandler.clearBlockPos();
                SyncPlayerPacket packet = new SyncPlayerPacket(false, BlockPos.ZERO, placerHandler.getDistanceWarped());
                CheckpointPacketHandler.INSTANCE.sendToServer(packet);
            } else {
                // Add to the player capability
                placerHandler.setBlockPos(pos);
                SyncPlayerPacket packet = new SyncPlayerPacket(true, pos, placerHandler.getDistanceWarped());
                CheckpointPacketHandler.INSTANCE.sendToServer(packet);
            }
        });
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        worldIn.setBlockState(pos.up(), BlockRegister.CHECKPOINT_UPPER.get().getDefaultState(), 3);

        if (placer == null) return;

        if (tileEntity instanceof CheckpointTileEntity) {
            tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(entityHandler -> entityHandler.setPlayerId(placer.getUniqueID()));
            tileEntity.markDirty();
        }

        this.setPlayerPair(worldIn, pos, placer, tileEntity);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        // Checks if both the ground below is solid and the block above isn't occupied
        // (i.e. there's a two-high space for the checkpoint to be placed)
        BlockPos under = pos.down();
        BlockState underState = worldIn.getBlockState(under);

        BlockPos above = pos.up();
        BlockState aboveState = worldIn.getBlockState(above);

        return underState.isSolidSide(worldIn, under, Direction.UP) && !aboveState.isSolid();
    }

    // Triggers whenever the player is using a pairer on a checkpoint
    // (i.e. right-clicking a checkpoint with a pairer)
    private void usePairer(World worldIn, BlockPos pos, PlayerEntity player, CheckpointTileEntity tileEntity, ItemStack interactedItem) {
        tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            if (handler.hasPair()) {
                // The checkpoint you're targeting is already paired
                TranslationTextComponent title = new TranslationTextComponent("action.already_paired");
                title.applyTextStyle(TextFormatting.RED);
                player.sendStatusMessage(title, true);
            } else if (!handler.isIdEmpty() && player.getUniqueID() != handler.getPlayerId()) {
                // The broken half is already in "pairing mode"
                TranslationTextComponent title = new TranslationTextComponent("action.pairing_mode");
                title.applyTextStyle(TextFormatting.RED);
                player.sendStatusMessage(title, true);
            } else {
                TranslationTextComponent title = new TranslationTextComponent("action.pairing_success");
                title.applyTextStyle(TextFormatting.GREEN);
                interactedItem.damageItem(1, player, entity -> {});
                this.setPlayerPair(worldIn, pos, player, tileEntity);
            }
        });
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            ItemStack interactedItem = player.getHeldItem(handIn);

            if (tileEntity instanceof CheckpointTileEntity && interactedItem.getItem() instanceof PairerItem) {
                this.usePairer(worldIn, pos, player, (CheckpointTileEntity) tileEntity, interactedItem);
            } else if (tileEntity instanceof CheckpointTileEntity) {
                INamedContainerProvider containerProvider = new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("container.checkpoint.checkpoint");
                    }

                    @Override
                    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new CheckpointContainer(i, worldIn, pos, playerInventory);
                    }
                };
                
                NetworkHooks.openGui((ServerPlayerEntity) player, containerProvider, tileEntity.getPos());
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
        }

        return ActionResultType.SUCCESS;
    }
}
