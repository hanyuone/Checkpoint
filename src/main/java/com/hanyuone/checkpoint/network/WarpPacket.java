package com.hanyuone.checkpoint.network;

import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class WarpPacket {
    private BlockPos entityLocation;
    private BlockPos destination;

    public WarpPacket(BlockPos entityLocation, BlockPos destination) {
        this.entityLocation = entityLocation;
        this.destination = destination;
    }

    public static void encode(WarpPacket packet, PacketBuffer buffer) {
        buffer.writeVarLong(packet.entityLocation.toLong());
        buffer.writeVarLong(packet.destination.toLong());
    }

    public static WarpPacket decode(PacketBuffer buffer) {
        BlockPos entityLocation = BlockPos.fromLong(buffer.readVarLong());
        BlockPos destination = BlockPos.fromLong(buffer.readVarLong());
        return new WarpPacket(entityLocation, destination);
    }

    public static class Handler {
        public static void handle(final WarpPacket message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();

                BlockPos location = message.entityLocation;
                TileEntity entity = player.getServerWorld().getTileEntity(location);

                if (entity instanceof CheckpointTileEntity) {
                    CheckpointTileEntity checkpointEntity = (CheckpointTileEntity) entity;
                    checkpointEntity.spendEnderPearls();
                }

                BlockPos position = message.destination;
                player.connection.setPlayerLocation(position.getX(), position.getY(), position.getZ(), player.rotationYaw, player.rotationPitch);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
