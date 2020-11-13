package com.hanyuone.checkpoint.network;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientSyncCheckpointPacket {
    BlockPos entityPos;

    BlockPos pos;
    boolean hasPair;

    UUID playerId;
    int chargingPearls;

    public ClientSyncCheckpointPacket(BlockPos entityPos, BlockPos pos, boolean hasPair, UUID playerId, int chargingPearls) {
        this.entityPos = entityPos;
        this.pos = pos;
        this.hasPair = hasPair;
        this.playerId = playerId;
        this.chargingPearls = chargingPearls;
    }

    public static void encode(ClientSyncCheckpointPacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.entityPos);
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.hasPair);
        buffer.writeUniqueId(packet.playerId);
        buffer.writeInt(packet.chargingPearls);
    }

    public static ClientSyncCheckpointPacket decode(PacketBuffer buffer) {
        BlockPos entityPos = buffer.readBlockPos();
        BlockPos pos = buffer.readBlockPos();
        boolean hasPair = buffer.readBoolean();
        UUID playerId = buffer.readUniqueId();
        int chargingPearls = buffer.readInt();

        return new ClientSyncCheckpointPacket(entityPos, pos, hasPair, playerId, chargingPearls);
    }

    public static void handle(ClientSyncCheckpointPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> handleOnClient(message)));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(ClientSyncCheckpointPacket message) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        TileEntity entity = player.getEntityWorld().getTileEntity(message.entityPos);

        if (entity instanceof CheckpointTileEntity) {
            CheckpointTileEntity checkpointEntity = (CheckpointTileEntity) entity;

            checkpointEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
                if (message.hasPair) {
                    handler.setBlockPos(message.pos);
                } else {
                    handler.clearBlockPos();
                }

                handler.setPlayerId(message.playerId);
                handler.setChargingPearls(message.chargingPearls);
            });
        }
    }
}
