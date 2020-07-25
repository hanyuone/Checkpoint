package com.hanyuone.checkpoint.network;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.capability.player.PlayerPairProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientSyncPairPacket {
    boolean hasPair;
    BlockPos pos;

    public ClientSyncPairPacket(boolean hasPair, BlockPos pos) {
        this.hasPair = hasPair;
        this.pos = pos;
    }

    public static void encode(ClientSyncPairPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.hasPair);
        buffer.writeLong(packet.pos.toLong());
    }

    public static ClientSyncPairPacket decode(PacketBuffer buffer) {
        boolean hasPair = buffer.readBoolean();
        BlockPos pos = BlockPos.fromLong(buffer.readLong());

        return new ClientSyncPairPacket(hasPair, pos);
    }

    public static void handle(ClientSyncPairPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> handleOnClient(message)));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(ClientSyncPairPacket message) {
        ClientPlayerEntity player = Minecraft.getInstance().player;

        player.getCapability(PlayerPairProvider.PLAYER_PAIR).ifPresent(handler -> {
            if (message.hasPair) {
                handler.setBlockPos(message.pos);
            } else {
                handler.clearBlockPos();
            }
        });
    }
}
