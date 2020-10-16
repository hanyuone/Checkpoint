package com.hanyuone.checkpoint.util;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import com.hanyuone.checkpoint.network.CheckpointPacketHandler;
import com.hanyuone.checkpoint.network.ClientSyncPlayerPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
}
