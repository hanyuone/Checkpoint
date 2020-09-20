package com.hanyuone.checkpoint.network;

import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import com.hanyuone.checkpoint.advancement.Advancements;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class WarpPacket {
    private final BlockPos entityLocation;
    private final BlockPos destination;

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

            // Spend ender pearls on the checkpoint you're warping from
            BlockPos location = message.entityLocation;
            TileEntity entity = player.getServerWorld().getTileEntity(location);

            if (entity instanceof CheckpointTileEntity) {
                CheckpointTileEntity checkpointEntity = (CheckpointTileEntity) entity;
                checkpointEntity.spendEnderPearls();
            }

            BlockPos destination = message.destination;

            // Adjust to make the player stand in the centre of the block
            player.connection.setPlayerLocation(destination.getX() + 0.5f, destination.getY(), destination.getZ() + 0.5f, player.rotationYaw, player.rotationPitch);

            // Update player achievements/stats
            player.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(handler -> {
                int distance = (int) Math.sqrt(location.distanceSq(destination));
                int newDistance = handler.getDistanceWarped() + distance;

                handler.setDistanceWarped(newDistance);
                Advancements.WARP_DISTANCE.trigger(player, newDistance);

                // Sync player stats in server and client, should be done last
                ClientSyncPlayerPacket packet = new ClientSyncPlayerPacket(handler.hasPair(), handler.getBlockPos(), newDistance);
                CheckpointPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
            });

            // Add particles and sounds when the player is warped
            Minecraft mc = Minecraft.getInstance();
            mc.world.playSound(player, destination, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.AMBIENT, 1f, 1f);

            for (int i = 0; i < 128; i++) {
                mc.world.addParticle(
                        ParticleTypes.PORTAL,
                        (destination.getX() + 0.5) + (mc.world.rand.nextDouble() - 0.5) * 3,
                        destination.getY() + (mc.world.rand.nextDouble() - 0.5) * 3,
                        (destination.getZ() + 0.5) + (mc.world.rand.nextDouble() - 0.5) * 3,
                        (mc.world.rand.nextDouble() - 0.5) * 2,
                        -mc.world.rand.nextDouble(),
                        (mc.world.rand.nextDouble() - 0.5) * 2
                );
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
