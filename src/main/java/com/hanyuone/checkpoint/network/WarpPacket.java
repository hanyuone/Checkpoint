package com.hanyuone.checkpoint.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class WarpPacket {
    private BlockPos pos;

    public WarpPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(WarpPacket packet, PacketBuffer buffer) {
        buffer.writeVarLong(packet.pos.toLong());
    }

    public static WarpPacket decode(PacketBuffer buffer) {
        return new WarpPacket(BlockPos.fromLong(buffer.readVarLong()));
    }

    public static class Handler {
        public static void handle(final WarpPacket message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                BlockPos position = message.pos;
                player.connection.setPlayerLocation(position.getX(), position.getY(), position.getZ(), player.rotationYaw, player.rotationPitch);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
