package com.hanyuone.checkpoint;

import com.hanyuone.checkpoint.advancement.WarpDistanceTrigger;
import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairHandler;
import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairStorage;
import com.hanyuone.checkpoint.capability.checkpoint.ICheckpointPair;
import com.hanyuone.checkpoint.capability.player.IPlayerCapability;
import com.hanyuone.checkpoint.capability.player.PlayerCapabilityHandler;
import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import com.hanyuone.checkpoint.capability.player.PlayerCapabilityStorage;
import com.hanyuone.checkpoint.client.ClientHandler;
import com.hanyuone.checkpoint.network.CheckpointPacketHandler;
import com.hanyuone.checkpoint.util.Advancements;
import com.hanyuone.checkpoint.util.RegistryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("checkpoint")
public class Checkpoint {
    public static final String MOD_ID = "checkpoint";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public Checkpoint() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

        RegistryHandler.init();
        CheckpointPacketHandler.register();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(ICheckpointPair.class, new CheckpointPairStorage(), CheckpointPairHandler::new);
        CapabilityManager.INSTANCE.register(IPlayerCapability.class, new PlayerCapabilityStorage(), PlayerCapabilityHandler::new);

        Advancements.WARP_DISTANCE = (WarpDistanceTrigger) Advancements.register(new WarpDistanceTrigger());
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        ClientHandler.init();
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(new ResourceLocation(Checkpoint.MOD_ID, "player_pair"), new PlayerCapabilityProvider());
        }
    }

    public static final ItemGroup TAB = new ItemGroup("checkpointTab") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.ENDER_EYE);
        }
    };
}
