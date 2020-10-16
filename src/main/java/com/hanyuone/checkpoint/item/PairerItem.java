package com.hanyuone.checkpoint.item;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.block.CheckpointBlock;
import com.hanyuone.checkpoint.block.UpperBlock;
import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.capability.player.PlayerCapabilityProvider;
import com.hanyuone.checkpoint.client.ClientHandler;
import com.hanyuone.checkpoint.register.BlockRegister;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicBoolean;

public class PairerItem extends Item {
    public PairerItem() {
        super(new Item.Properties()
                .group(Checkpoint.TAB)
                .maxStackSize(1)
                .defaultMaxDamage(16));
    }

    // Triggers whenever the player is using a pairer on a checkpoint
    // (i.e. right-clicking a checkpoint with a pairer)
    private void usePairer(World worldIn, BlockPos pos, PlayerEntity player, CheckpointTileEntity tileEntity, ItemStack interactedItem) {
        tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            if (handler.hasPair()) {
                // The checkpoint you're targeting is already paired
                ClientHandler.displayNotification(player, "action.already_paired", TextFormatting.RED);
            } else if (!handler.isIdEmpty() && player.getUniqueID() != handler.getPlayerId()) {
                // The broken half is already in "pairing mode"
                ClientHandler.displayNotification(player, "action.pairing_mode", TextFormatting.RED);
            } else {
                ClientHandler.displayNotification(player, "action.pairing_success", TextFormatting.GREEN);
                interactedItem.damageItem(1, player, entity -> {});
                ((CheckpointBlock)BlockRegister.CHECKPOINT.get()).setPlayerPair(worldIn, pos, player, tileEntity);
            }
        });
    }

    private static boolean isTooFar(BlockPos pos, BlockPos dest) {
        return !pos.withinDistance(dest, Items.ENDER_PEARL.getMaxStackSize() * 100);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();

        Block targetBlock = world.getBlockState(pos).getBlock();

        if (targetBlock instanceof CheckpointBlock || targetBlock instanceof UpperBlock) {
            PlayerEntity player = context.getPlayer();
            AtomicBoolean hasFailed = new AtomicBoolean(false);
            BlockPos finalPos;

            // Makes sure the actual position is on the lower half of the checkpoint
            if (targetBlock instanceof UpperBlock) {
                finalPos = pos.down();
            } else {
                finalPos = pos;
            }

            TileEntity tileEntity = world.getTileEntity(finalPos);

            player.getCapability(PlayerCapabilityProvider.PLAYER_CAPABILITY, null).ifPresent(handler -> {
                if (!handler.hasPair()) {
                    ClientHandler.displayNotification(player, "action.initiate_pair", TextFormatting.GREEN);
                } else if (handler.hasPair() && isTooFar(finalPos, handler.getBlockPos())) {
                    if (world.isRemote) {
                        ClientHandler.displayNotification(player, "action.too_far", TextFormatting.RED);
                        hasFailed.set(true);
                    }
                } else {
                    this.usePairer(world, finalPos, player, (CheckpointTileEntity) tileEntity, stack);
                }
            });
        }

        return ActionResultType.SUCCESS;
    }
}
