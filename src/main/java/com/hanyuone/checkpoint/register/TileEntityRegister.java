package com.hanyuone.checkpoint.register;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import com.hanyuone.checkpoint.tileentity.UpperTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class TileEntityRegister {
    public static final DeferredRegister<TileEntityType<?>> REGISTER = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Checkpoint.MOD_ID);

    public static final RegistryObject<TileEntityType<CheckpointTileEntity>> CHECKPOINT_TILE_ENTITY = register(
            "checkpoint",
            CheckpointTileEntity::new,
            () -> new Block[] {BlockRegister.CHECKPOINT.get()}
    );

    public static final RegistryObject<TileEntityType<UpperTileEntity>> UPPER_TILE_ENTITY = register(
            "checkpoint_upper",
            UpperTileEntity::new,
            () -> new Block[] {BlockRegister.CHECKPOINT_UPPER.get()}
    );

    private static <T extends TileEntity> RegistryObject<TileEntityType<T>> register(String id, Supplier<T> factoryIn, Supplier<Block[]> validBlocksSupplier) {
        return REGISTER.register(id, () -> TileEntityType.Builder.create(factoryIn, validBlocksSupplier.get()).build(null));
    }
}
