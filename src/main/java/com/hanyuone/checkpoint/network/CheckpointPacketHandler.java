package com.hanyuone.checkpoint.network;

import com.hanyuone.checkpoint.Checkpoint;
import net.minecraft.util.ResourceLocation;
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
        INSTANCE.registerMessage(0, WarpPacket.class, WarpPacket::encode, WarpPacket::decode, WarpPacket.Handler::handle);
    }
}
