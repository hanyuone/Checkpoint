package com.hanyuone.checkpoint.network;

import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import com.hanyuone.checkpoint.advancement.Advancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class WarpPacket {
    private BlockPos entityLocation;
    private BlockPos destination;

    public WarpPacket(BlockPos entityLocation, BlockPos destination) {
        this.entityLocation = entityLocation;
        this.destination = destination;
    }

    public static void encode(WarpPacket packet, PacketBuffer buffer) {
        buffer.writeLong(packet.entityLocation.toLong());
        buffer.writeLong(packet.destination.toLong());
    }

    public static WarpPacket decode(PacketBuffer buffer) {
        BlockPos entityLocation = BlockPos.fromLong(buffer.readLong());
        BlockPos destination = BlockPos.fromLong(buffer.readLong());

        return new WarpPacket(entityLocation, destination);
    }

    public static void handle(final WarpPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();

            BlockPos location = message.entityLocation;
            TileEntity entity = player.getServerWorld().getTileEntity(location);

            if (entity instanceof CheckpointTileEntity) {
                CheckpointTileEntity checkpointEntity = (CheckpointTileEntity) entity;
                checkpointEntity.spendEnderPearls();
            }

            BlockPos destination = message.destination;

            // Adjust to make the player stand in the centre of the block
            player.connection.setPlayerLocation(destination.getX() + 0.5f, destination.getY(), destination.getZ() + 0.5f, player.rotationYaw, player.rotationPitch);

            player.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(handler -> {
                int distance = (int) Math.sqrt(location.distanceSq(destination));
                int newDistance = handler.getDistanceWarped() + distance;

                handler.setDistanceWarped(newDistance);
                Advancements.WARP_DISTANCE.trigger(player, newDistance);

                // Sync player stats in server and client, should be done last
                ClientSyncPlayerPacket packet = new ClientSyncPlayerPacket(handler.hasPair(), handler.getBlockPos(), newDistance);
                CheckpointPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
            });
        });

        ctx.get().setPacketHandled(true);
    }
}
