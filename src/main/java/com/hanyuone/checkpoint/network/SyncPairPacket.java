package com.hanyuone.checkpoint.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class SyncPairPacket {
    boolean hasPair;
    BlockPos pos;

    public SyncPairPacket(boolean hasPair, BlockPos pos) {
        this.hasPair = hasPair;
        this.pos = pos;
    }

    public static void encode(SyncPairPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.hasPair);
        buffer.writeLong(packet.pos.toLong());
    }

    public static SyncPairPacket decode(PacketBuffer buffer) {
        boolean hasPair = buffer.readBoolean();
        BlockPos pos = BlockPos.fromLong(buffer.readLong());

        return new SyncPairPacket(hasPair, pos);
    }

    public static void handle(final SyncPairPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientSyncPairPacket clientPacket = new ClientSyncPairPacket(message.hasPair, message.pos);
            CheckpointPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), clientPacket);
        });

        ctx.get().setPacketHandled(true);
    }
}
