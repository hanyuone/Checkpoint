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

        INSTANCE.messageBuilder(SyncPairPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SyncPairPacket::encode)
                .decoder(SyncPairPacket::decode)
                .consumer(SyncPairPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientSyncPairPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientSyncPairPacket::encode)
                .decoder(ClientSyncPairPacket::decode)
                .consumer(ClientSyncPairPacket::handle)
                .add();
    }
}
