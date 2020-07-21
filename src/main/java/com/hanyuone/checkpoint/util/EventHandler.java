package com.hanyuone.checkpoint.util;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.capability.CheckpointPairProvider;
import com.hanyuone.checkpoint.network.CheckpointPacketHandler;
import com.hanyuone.checkpoint.network.SyncPairPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Checkpoint.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();

        player.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            SyncPairPacket packet = new SyncPairPacket(handler.hasPair(), handler.getBlockPos());
            CheckpointPacketHandler.INSTANCE.sendToServer(packet);
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerEntity player = event.getPlayer();

        player.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            SyncPairPacket packet = new SyncPairPacket(handler.hasPair(), handler.getBlockPos());
            CheckpointPacketHandler.INSTANCE.sendToServer(packet);
        });
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        PlayerEntity player = event.getPlayer();

        player.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            SyncPairPacket packet = new SyncPairPacket(handler.hasPair(), handler.getBlockPos());
            CheckpointPacketHandler.INSTANCE.sendToServer(packet);
        });
    }
}
