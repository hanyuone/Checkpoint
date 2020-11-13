package com.hanyuone.checkpoint.network;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class ChargePacket {
    BlockPos entityLocation;

    public ChargePacket(BlockPos entityLocation) {
        this.entityLocation = entityLocation;
    }
    public static void encode(ChargePacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.entityLocation);
    }

    public static ChargePacket decode(PacketBuffer buffer) {
        BlockPos entityLocation = buffer.readBlockPos();

        return new ChargePacket(entityLocation);
    }

    public static void handle(ChargePacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();

            BlockPos location = message.entityLocation;

            TileEntity entity = player.getServerWorld().getTileEntity(location);
            if (entity instanceof CheckpointTileEntity) {
                CheckpointTileEntity checkpointEntity = (CheckpointTileEntity) entity;
                checkpointEntity.spendEnderPearls();

                checkpointEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
                    ClientSyncCheckpointPacket clientPacket = new ClientSyncCheckpointPacket(location, handler.getBlockPos(), handler.hasPair(), handler.getPlayerId(), handler.getChargingPearls());
                    CheckpointPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), clientPacket);
                });
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
