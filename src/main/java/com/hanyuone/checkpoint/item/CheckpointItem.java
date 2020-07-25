package com.hanyuone.checkpoint.item;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.capability.player.PlayerPairProvider;
import com.hanyuone.checkpoint.register.BlockRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class CheckpointItem extends BlockItem {
    public CheckpointItem() {
        super(BlockRegister.CHECKPOINT.get(), new Item.Properties().group(Checkpoint.TAB));
    }

    private String formatBlockPos(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        ClientPlayerEntity player = Minecraft.getInstance().player;

        if (player != null) {
            player.getCapability(PlayerPairProvider.PLAYER_PAIR, null).ifPresent(handler -> {
                StringTextComponent component;

                if (handler.hasPair()) {
                    String formatted = formatBlockPos(handler.getBlockPos());
                    component = new StringTextComponent(I18n.format("tooltip.checkpoint.pair_location", formatted));
                    component.applyTextStyle(TextFormatting.GREEN);
                } else {
                    component = new StringTextComponent(I18n.format("tooltip.checkpoint.not_paired"));
                    component.applyTextStyle(TextFormatting.RED);
                }

                tooltip.add(component);
            });
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
