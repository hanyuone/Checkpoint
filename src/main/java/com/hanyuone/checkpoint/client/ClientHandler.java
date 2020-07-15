package com.hanyuone.checkpoint.client;

import com.hanyuone.checkpoint.register.ContainerRegister;
import net.minecraft.client.gui.ScreenManager;

public class ClientHandler {
    public static void init() {
        ScreenManager.registerFactory(ContainerRegister.CHECKPOINT.get(), CheckpointScreen::new);
    }
}
