package com.hanyuone.checkpoint.network;

import com.hanyuone.checkpoint.Checkpoint;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class CheckpointPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Checkpoint.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int index = 0;

        INSTANCE.messageBuilder(WarpPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(WarpPacket::encode)
                .decoder(WarpPacket::decode)
                .consumer(WarpPacket::handle)
                .add();

        INSTANCE.messageBuilder(ChargePacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChargePacket::encode)
                .decoder(ChargePacket::decode)
                .consumer(ChargePacket::handle)
                .add();

        INSTANCE.messageBuilder(SyncPlayerPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SyncPlayerPacket::encode)
                .decoder(SyncPlayerPacket::decode)
                .consumer(SyncPlayerPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientSyncCheckpointPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientSyncCheckpointPacket::encode)
                .decoder(ClientSyncCheckpointPacket::decode)
                .consumer(ClientSyncCheckpointPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientSyncPlayerPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientSyncPlayerPacket::encode)
                .decoder(ClientSyncPlayerPacket::decode)
                .consumer(ClientSyncPlayerPacket::handle)
                .add();
    }
}
