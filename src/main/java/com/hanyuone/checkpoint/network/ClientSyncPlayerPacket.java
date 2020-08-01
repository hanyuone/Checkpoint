package com.hanyuone.checkpoint.network;

import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientSyncPlayerPacket {
    boolean hasPair;
    BlockPos pos;

    int distanceWarped;

    public ClientSyncPlayerPacket(boolean hasPair, BlockPos pos, int distance) {
        this.hasPair = hasPair;
        this.pos = pos;
        this.distanceWarped = distance;
    }

    public static void encode(ClientSyncPlayerPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.hasPair);
        buffer.writeLong(packet.pos.toLong());
        buffer.writeInt(packet.distanceWarped);
    }

    public static ClientSyncPlayerPacket decode(PacketBuffer buffer) {
        boolean hasPair = buffer.readBoolean();
        BlockPos pos = BlockPos.fromLong(buffer.readLong());
        int distance = buffer.readInt();

        return new ClientSyncPlayerPacket(hasPair, pos, distance);
    }

    public static void handle(ClientSyncPlayerPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> handleOnClient(message)));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(ClientSyncPlayerPacket message) {
        ClientPlayerEntity player = Minecraft.getInstance().player;

        player.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY).ifPresent(handler -> {
            if (message.hasPair) {
                handler.setBlockPos(message.pos);
            } else {
                handler.clearBlockPos();
            }

            handler.setDistanceWarped(message.distanceWarped);
        });
    }
}
