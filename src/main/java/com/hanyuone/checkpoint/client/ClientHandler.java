package com.hanyuone.checkpoint.client;

import com.hanyuone.checkpoint.client.gui.CheckpointScreen;
import com.hanyuone.checkpoint.client.render.UpperRenderer;
import com.hanyuone.checkpoint.register.ContainerRegister;
import com.hanyuone.checkpoint.register.TileEntityRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientHandler {
    public static void init() {
        ScreenManager.registerFactory(ContainerRegister.CHECKPOINT.get(), CheckpointScreen::new);

        ClientRegistry.bindTileEntityRenderer(TileEntityRegister.UPPER_TILE_ENTITY.get(), UpperRenderer::new);
    }

    public static void displayNotification(PlayerEntity player, String key, TextFormatting format) {
        TranslationTextComponent title = new TranslationTextComponent(key);
        title.applyTextStyle(format);
        player.sendStatusMessage(title, true);
    }

    public static void displayError(PlayerEntity player, String key) {
        ClientHandler.displayNotification(player, key, TextFormatting.RED);
        Minecraft mc = Minecraft.getInstance();
        mc.world.playSound(player, player.getPosition(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.AMBIENT, 1f, 1f);
    }

    public static void displaySuccess(PlayerEntity player, String key) {
        ClientHandler.displayNotification(player, key, TextFormatting.GREEN);
        Minecraft mc = Minecraft.getInstance();
        mc.world.playSound(player, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT, 1f, 1f);
    }
}
