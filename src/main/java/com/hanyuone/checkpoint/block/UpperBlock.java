package com.hanyuone.checkpoint.block;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.tileentity.UpperTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.Random;

public class UpperBlock extends Block {
    public static final VoxelShape UPPER = VoxelShapes.or(
            makeCuboidShape(4, 0, 4, 12, 7, 12),
            makeCuboidShape(2, 7, 2, 14, 9, 14),
            makeCuboidShape(5, 9, 5, 11, 10, 11)
    );

    public UpperBlock() {
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
        return new UpperTileEntity();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return UPPER;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos lowerPos = pos.down();
        BlockState lowerState = worldIn.getBlockState(lowerPos);
        Block lowerBlock = lowerState.getBlock();

        if (lowerBlock instanceof CheckpointBlock) {
            lowerBlock.onBlockHarvested(worldIn, lowerPos, lowerState, player);
            worldIn.setBlockState(lowerPos, Blocks.AIR.getDefaultState(), 35);
        } else {
            super.onBlockHarvested(worldIn, pos, state, player);
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos under = pos.down();
        return worldIn.getBlockState(under).getBlock() instanceof CheckpointBlock;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        BlockState lowerState = worldIn.getBlockState(pos.down());
        Block lowerBlock = lowerState.getBlock();

        if (lowerBlock instanceof CheckpointBlock) {
            return lowerBlock.onBlockActivated(lowerState, worldIn, pos.down(), player, handIn, hit);
        }

        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.animateTick(stateIn, worldIn, pos, rand);

        UpperTileEntity tileEntity = (UpperTileEntity) worldIn.getTileEntity(pos);

        tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            if (!handler.hasPair()) return;

            // Copied from nether portal block
            for (int i = 0; i < 4; ++i) {
                int j = rand.nextInt(2) * 2 - 1;

                double x = (double)pos.getX() + 0.5D + 0.25D * (double)j;
                double y = (double)pos.getY() + (double)rand.nextFloat();
                double z = (double)pos.getZ() + (double)rand.nextFloat();

                double xSpeed = rand.nextFloat() * 2.0F * (float)j;
                double ySpeed = ((double)rand.nextFloat() - 0.5D) * 0.5D;
                double zSpeed = ((double)rand.nextFloat() - 0.5D) * 0.5D;

                worldIn.addParticle(ParticleTypes.ENCHANT, x, y, z, xSpeed, ySpeed, zSpeed);
            }
        });
    }
}
