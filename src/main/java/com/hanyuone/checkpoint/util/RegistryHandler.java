package com.hanyuone.checkpoint.util;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.register.BlockRegister;
import com.hanyuone.checkpoint.register.ContainerRegister;
import com.hanyuone.checkpoint.register.ItemRegister;
import com.hanyuone.checkpoint.register.TileEntityRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid=Checkpoint.MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {
    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemRegister.REGISTER.register(bus);
        BlockRegister.REGISTER.register(bus);
        TileEntityRegister.REGISTER.register(bus);
        ContainerRegister.REGISTER.register(bus);
    }

    @SubscribeEvent
    public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
        }
    }
}
