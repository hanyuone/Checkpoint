package com.hanyuone.checkpoint.block;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.capability.checkpoint.ICheckpointPair;
import com.hanyuone.checkpoint.capability.player.PlayerPairProvider;
import com.hanyuone.checkpoint.container.CheckpointContainer;
import com.hanyuone.checkpoint.network.CheckpointPacketHandler;
import com.hanyuone.checkpoint.network.SyncPairPacket;
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
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class CheckpointBlock extends ContainerBlock {
    public static final EnumProperty<CheckpointHalf> HALF = EnumProperty.create("half", CheckpointHalf.class);

    public static final VoxelShape LOWER = VoxelShapes.or(
            makeCuboidShape(1, 0, 1, 15, 2, 15),
            makeCuboidShape(2, 2, 2, 14, 3, 14),
            makeCuboidShape(4, 3, 4, 12, 15, 12)
    );

    public static final VoxelShape UPPER = VoxelShapes.or(
            makeCuboidShape(4, 0, 4, 12, 7, 12),
            makeCuboidShape(2, 7, 2, 14, 9, 14),
            makeCuboidShape(5, 9, 5, 11, 10, 11),
            makeCuboidShape(6, 11, 6, 10, 15, 10)
    );

    public CheckpointBlock() {
        super(Block.Properties.create(Material.ROCK)
                .hardnessAndResistance(1.5f, 6)
                .sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE));

        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(HALF, CheckpointHalf.LOWER));
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new CheckpointTileEntity();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return state.get(HALF) == CheckpointHalf.LOWER ? LOWER : UPPER;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, Blocks.AIR.getDefaultState(), te, stack);
    }

    @Override
    public void onBlockHarvested(World worldIn, @Nonnull BlockPos pos, BlockState state, @Nonnull PlayerEntity player) {
        CheckpointHalf half = state.get(HALF);
        // If you're currently mining the lower half, find the upper half,
        // otherwise, find the lower half
        BlockPos otherPos = half == CheckpointHalf.LOWER ? pos.up() : pos.down();
        BlockState blockState = worldIn.getBlockState(otherPos);

        TileEntity checkpointEntity = worldIn.getTileEntity(pos);

        // Checks if the other half of the checkpoint we want to destroy is
        // *actually* the other half
        if (blockState.getBlock() == this && blockState.get(HALF) != half) {
            worldIn.setBlockState(otherPos, Blocks.AIR.getDefaultState(), 35);
            worldIn.playEvent(player, 2001, otherPos, Block.getStateId(blockState));
            ItemStack itemStack = player.getHeldItemMainhand();

            if (!worldIn.isRemote && !player.isCreative() && player.canHarvestBlock(blockState)) {
                Block.spawnDrops(state, worldIn, pos, null, player, itemStack);
                Block.spawnDrops(blockState, worldIn, otherPos, null, player, itemStack);

                ItemStack enderPearls = new ItemStack(Items.ENDER_PEARL, ((CheckpointTileEntity) checkpointEntity).getEnderPearls());
                worldIn.addEntity(new ItemEntity(worldIn, pos.getX(), pos.getY(), pos.getZ(), enderPearls));
            }
        }

        // Disable the other half of the checkpoint pair
        checkpointEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            if (handler.hasPair()) {
                TileEntity otherEntity = worldIn.getTileEntity(handler.getBlockPos());
                otherEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(ICheckpointPair::clearBlockPos);
            }

            UUID playerId = handler.getPlayerId();

            if (playerId != null) {
                PlayerEntity playerFromEntity = worldIn.getPlayerByUuid(playerId);

                // If the checkpoint was just made, delete the saved data on the player
                // so the next checkpoint doesn't point to a dead BlockPos
                playerFromEntity.getCapability(PlayerPairProvider.PLAYER_PAIR, null).ifPresent(playerHandler -> {
                    if (playerHandler.hasPair() && playerHandler.getBlockPos().equals(pos)) {
                        playerHandler.clearBlockPos();
                        SyncPairPacket packet = new SyncPairPacket(false, BlockPos.ZERO);
                        CheckpointPacketHandler.INSTANCE.sendToServer(packet);
                    }
                });
            }
        });

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(entityHandler -> {
            entityHandler.setPlayerId(placer.getUniqueID());
        });

        placer.getCapability(PlayerPairProvider.PLAYER_PAIR, null).ifPresent(placerHandler -> {
            if (placerHandler.hasPair() && !placerHandler.getBlockPos().equals(pos)) {
                BlockPos oldPos = placerHandler.getBlockPos();
                TileEntity oldEntity = worldIn.getTileEntity(oldPos);

                if (oldEntity instanceof CheckpointTileEntity) {
                    tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(entityHandler -> {
                        entityHandler.setBlockPos(oldPos);
                    });

                    oldEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(oldHandler -> {
                        oldHandler.setBlockPos(pos);
                    });
                }

                placerHandler.clearBlockPos();
                SyncPairPacket packet = new SyncPairPacket(false, BlockPos.ZERO);
                CheckpointPacketHandler.INSTANCE.sendToServer(packet);
            } else {
                placerHandler.setBlockPos(pos);
                SyncPairPacket packet = new SyncPairPacket(true, pos);
                CheckpointPacketHandler.INSTANCE.sendToServer(packet);
            }
        });

        worldIn.setBlockState(pos.up(), state.with(HALF, CheckpointHalf.UPPER));
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        if (state.get(HALF) == CheckpointHalf.LOWER) {
            return true;
        }

        BlockState under = worldIn.getBlockState(pos.down());
        return under.getBlock() == this && under.get(HALF) == CheckpointHalf.LOWER;
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            // Redirecting all upper-half `CheckpointBlock` interactions to display the GUI
            // of the lower half instead, since the lower half is what stores all the relevant
            // information
            BlockPos lowerPos = state.get(HALF) == CheckpointHalf.UPPER ? pos.down() : pos;
            TileEntity tileEntity = worldIn.getTileEntity(lowerPos);

            if (tileEntity instanceof CheckpointTileEntity) {
                INamedContainerProvider containerProvider = new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("container.checkpoint.checkpoint");
                    }

                    @Override
                    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new CheckpointContainer(i, worldIn, lowerPos, playerInventory);
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
