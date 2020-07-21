package com.hanyuone.checkpoint.item;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.capability.CheckpointPairProvider;
import com.hanyuone.checkpoint.register.BlockRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class CheckpointItem extends BlockItem {
    public CheckpointItem() {
        super(BlockRegister.CHECKPOINT.get(), new Item.Properties().group(Checkpoint.TAB));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        ClientPlayerEntity player = Minecraft.getInstance().player;

        if (player != null) {
            player.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
                if (handler.hasPair()) {
                    tooltip.add(new StringTextComponent("Will pair up to " + handler.getBlockPos().toString()));
                } else {
                    tooltip.add(new StringTextComponent("Not paired up!"));
                }
            });
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
