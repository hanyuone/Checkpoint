package com.hanyuone.checkpoint.block;

import com.hanyuone.checkpoint.tileentity.UpperTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
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

public class UpperBlock extends Block {
    public static final VoxelShape UPPER = VoxelShapes.or(
            makeCuboidShape(4, 0, 4, 12, 7, 12),
            makeCuboidShape(2, 7, 2, 14, 9, 14),
            makeCuboidShape(5, 9, 5, 11, 10, 11),
            makeCuboidShape(6, 11, 6, 10, 15, 10)
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
        BlockState lowerState = worldIn.getBlockState(pos.down());

        if (lowerState.getBlock() instanceof CheckpointBlock) {
            worldIn.destroyBlock(pos.down(), false);
        }

        super.onBlockHarvested(worldIn, pos, state, player);
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
}
