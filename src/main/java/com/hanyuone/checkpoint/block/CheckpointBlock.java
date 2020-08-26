package com.hanyuone.checkpoint.block;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import com.hanyuone.checkpoint.container.CheckpointContainer;
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

// TODO: separate logic into two separate blocks!
public class CheckpointBlock extends Block {
    public static final VoxelShape LOWER = VoxelShapes.or(
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

        if (checkpointEntity instanceof CheckpointTileEntity) {
            ((CheckpointTileEntity) checkpointEntity).disablePair(worldIn, pos);
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        worldIn.setBlockState(pos.up(), BlockRegister.CHECKPOINT_UPPER.get().getDefaultState());

        if (placer == null) return;

        if (tileEntity instanceof CheckpointTileEntity) {
            tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(entityHandler -> entityHandler.setPlayerId(placer.getUniqueID()));
        }

        placer.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(placerHandler -> {
            if (placerHandler.hasPair() && !placerHandler.getBlockPos().equals(pos)) {
                BlockPos oldPos = placerHandler.getBlockPos();
                TileEntity oldEntity = worldIn.getTileEntity(oldPos);

                if (tileEntity instanceof CheckpointTileEntity && oldEntity instanceof CheckpointTileEntity) {
                    tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(entityHandler -> {
                        entityHandler.setBlockPos(oldPos);
                    });

                    oldEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(oldHandler -> {
                        oldHandler.setBlockPos(pos);
                    });
                }

                placerHandler.clearBlockPos();
                SyncPlayerPacket packet = new SyncPlayerPacket(false, BlockPos.ZERO, placerHandler.getDistanceWarped());
                CheckpointPacketHandler.INSTANCE.sendToServer(packet);
            } else {
                placerHandler.setBlockPos(pos);
                SyncPlayerPacket packet = new SyncPlayerPacket(true, pos, placerHandler.getDistanceWarped());
                CheckpointPacketHandler.INSTANCE.sendToServer(packet);
            }
        });
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);

            if (tileEntity instanceof CheckpointTileEntity) {
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
