package com.hanyuone.checkpoint.register;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.block.CheckpointBlock;
import com.hanyuone.checkpoint.block.UpperBlock;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegister {
    public static final DeferredRegister<Block> REGISTER = new DeferredRegister<>(ForgeRegistries.BLOCKS, Checkpoint.MOD_ID);

    public static final RegistryObject<Block> CHECKPOINT = REGISTER.register("checkpoint", CheckpointBlock::new);
    public static final RegistryObject<Block> CHECKPOINT_UPPER = REGISTER.register("checkpoint_upper", UpperBlock::new);
}
