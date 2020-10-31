package com.hanyuone.checkpoint.register;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.container.CheckpointContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerRegister {
    public static final DeferredRegister<ContainerType<?>> REGISTER = new DeferredRegister<>(ForgeRegistries.CONTAINERS, Checkpoint.MOD_ID);

    public static final RegistryObject<ContainerType<CheckpointContainer>> CHECKPOINT = REGISTER.register("checkpoint", () -> IForgeContainerType.create((id, inventory, data) -> {
        BlockPos pos = data.readBlockPos();
        World world = inventory.player.getEntityWorld();

        if (data.readBoolean()) {
            BlockPos suitablePos = data.readBlockPos();
            return new CheckpointContainer(id, world, pos, inventory, suitablePos);
        } else {
            return new CheckpointContainer(id, world, pos, inventory, null);
        }
    }));
}
