package com.hanyuone.checkpoint.client;

import com.hanyuone.checkpoint.client.gui.CheckpointScreen;
import com.hanyuone.checkpoint.client.render.UpperRenderer;
import com.hanyuone.checkpoint.register.ContainerRegister;
import com.hanyuone.checkpoint.register.TileEntityRegister;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientHandler {
    public static void init() {
        ScreenManager.registerFactory(ContainerRegister.CHECKPOINT.get(), CheckpointScreen::new);

        ClientRegistry.bindTileEntityRenderer(TileEntityRegister.UPPER_TILE_ENTITY.get(), UpperRenderer::new);
    }
}
