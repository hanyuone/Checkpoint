package com.hanyuone.checkpoint.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class SyncPlayerPacket {
    boolean hasPair;
    BlockPos pos;

    int distanceWarped;

    public SyncPlayerPacket(boolean hasPair, BlockPos pos, int distance) {
        this.hasPair = hasPair;
        this.pos = pos;

        this.distanceWarped = distance;
    }

    public static void encode(SyncPlayerPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.hasPair);
        buffer.writeLong(packet.pos.toLong());

        buffer.writeInt(packet.distanceWarped);
    }

    public static SyncPlayerPacket decode(PacketBuffer buffer) {
        boolean hasPair = buffer.readBoolean();
        BlockPos pos = BlockPos.fromLong(buffer.readLong());

        int distance = buffer.readInt();

        return new SyncPlayerPacket(hasPair, pos, distance);
    }

    public static void handle(final SyncPlayerPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientSyncPlayerPacket clientPacket = new ClientSyncPlayerPacket(message.hasPair, message.pos, message.distanceWarped);
            CheckpointPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), clientPacket);
        });

        ctx.get().setPacketHandled(true);
    }
}
