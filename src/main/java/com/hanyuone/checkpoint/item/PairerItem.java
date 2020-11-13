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
import net.minecraft.world.World;

public class PairerItem extends Item {
    public PairerItem() {
        super(new Item.Properties()
                .group(Checkpoint.TAB)
                .maxStackSize(1)
                .defaultMaxDamage(16));
    }

    // Checks to see if a checkpoint can be paired from a tile entity's perspective
    private void checkTileEntity(World worldIn, BlockPos pos, PlayerEntity player, CheckpointTileEntity tileEntity, ItemStack interactedItem, Boolean playerHasPair) {
        tileEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            if (handler.hasPair()) {
                // The checkpoint you're targeting is already paired
                ClientHandler.displayError(player, "action.already_paired");
            } else if (!handler.isIdEmpty() && player.getUniqueID() != handler.getPlayerId()) {
                // The broken half is already in "pairing mode"
                ClientHandler.displayError(player, "action.pairing_mode");
            } else if (!playerHasPair) {
                // The checks are successful, and we trigger this if the player doesn't already have
                // a checkpoint stored in memory
                ClientHandler.displaySuccess(player, "action.initiate_pair");
                ((CheckpointBlock) BlockRegister.CHECKPOINT.get()).setPlayerPair(worldIn, pos, player, tileEntity);
            } else {
                // We've successfully paired two checkpoints, we reduce durability and link the two
                // checkpoints together
                ClientHandler.displaySuccess(player, "action.pairing_success");
                interactedItem.damageItem(1, player, entity -> {});
                ((CheckpointBlock) BlockRegister.CHECKPOINT.get()).setPlayerPair(worldIn, pos, player, tileEntity);
            }
        });
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();

        Block targetBlock = world.getBlockState(pos).getBlock();

        if (targetBlock instanceof CheckpointBlock || targetBlock instanceof UpperBlock) {
            PlayerEntity player = context.getPlayer();
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
                    // Directly checks the tile entity capability, since the checks we have to perform are the same
                    this.checkTileEntity(world, finalPos, player, (CheckpointTileEntity) tileEntity, stack, false);
                } else {
                    // The player already has a checkpoint "remembered" in memory
                    if (handler.getBlockPos() == finalPos) {
                        ClientHandler.displayError(player, "action.pairing_itself");
                    } else {
                        // Interacts with the tile entity capability
                        this.checkTileEntity(world, finalPos, player, (CheckpointTileEntity) tileEntity, stack, true);
                    }
                }
            });
        }

        return ActionResultType.SUCCESS;
    }
}
