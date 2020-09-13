package com.hanyuone.checkpoint.util;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import com.hanyuone.checkpoint.network.CheckpointPacketHandler;
import com.hanyuone.checkpoint.network.ClientSyncPlayerPacket;
import com.hanyuone.checkpoint.register.ItemRegister;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Checkpoint.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();

        player.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(handler -> {
            ClientSyncPlayerPacket packet = new ClientSyncPlayerPacket(handler.hasPair(), handler.getBlockPos(), handler.getDistanceWarped());
            CheckpointPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerEntity player = event.getPlayer();

        player.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(handler -> {
            ClientSyncPlayerPacket packet = new ClientSyncPlayerPacket(handler.hasPair(), handler.getBlockPos(), handler.getDistanceWarped());
            CheckpointPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
        });
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        PlayerEntity player = event.getPlayer();

        player.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(handler -> {
            ClientSyncPlayerPacket packet = new ClientSyncPlayerPacket(handler.hasPair(), handler.getBlockPos(), handler.getDistanceWarped());
            CheckpointPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
        });
    }

    private static boolean isTooFar(BlockPos pos, BlockPos dest) {
        return !pos.withinDistance(dest, Items.ENDER_PEARL.getMaxStackSize() * 100);
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() != ItemRegister.CHECKPOINT_ITEM.get()) {
            return;
        }

        PlayerEntity player = event.getPlayer();

        player.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(handler -> {
            BlockPos currentPos = event.getPos();

            if (handler.hasPair() && isTooFar(currentPos, handler.getBlockPos())) {
                if (event.getWorld().isRemote) {
                    TranslationTextComponent title = new TranslationTextComponent("action.too_far");
                    title.applyTextStyles(TextFormatting.YELLOW, TextFormatting.BOLD);
                    player.sendStatusMessage(title, true);
                }

                event.setCanceled(true);
            }
        });
    }
}
