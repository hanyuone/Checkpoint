package com.hanyuone.checkpoint.client;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.capability.CheckpointPairProvider;
import com.hanyuone.checkpoint.container.CheckpointContainer;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class CheckpointScreen extends ContainerScreen<CheckpointContainer> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("checkpoint:textures/gui/container/checkpoint.png");
    private String costText = "";

    private PlayerEntity player;

    public CheckpointScreen(CheckpointContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.player = inv.player;
    }

    @Override
    protected void init() {
        super.init();

        TileEntity containerEntity = this.container.getTileEntity();

        // Display button
        String warpButtonText = "Warp";
        int buttonLeft = this.guiLeft + (this.xSize * 2 / 3) - (40 / 2);
        int buttonTop = this.guiTop + 32;

        Button warpButton = new Button(buttonLeft, buttonTop, 40, 20, warpButtonText, button -> {
            if (containerEntity instanceof CheckpointTileEntity) {
                // Assume hasPair() is always true
                containerEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
                    BlockPos pos = handler.getBlockPos();
                    // int newAmount = this.container.getEnderPearls() - ((CheckpointTileEntity) containerEntity).calculateCost();

                    // this.container.setEnderPearls(newAmount);
                    Checkpoint.LOGGER.debug(pos);

                    this.player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
                    this.player.setPositionAndRotation(pos.getX(), pos.getY(), pos.getZ(), this.player.rotationYaw, this.player.rotationPitch);
                });
            }
        });

        this.addButton(warpButton);

        if (containerEntity instanceof CheckpointTileEntity) {
            CheckpointTileEntity checkpointEntity = (CheckpointTileEntity) containerEntity;

            int cost = checkpointEntity.calculateCost();

            if (cost > 0) {
                this.costText = "Cost: " + cost;
            }

            containerEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
                warpButton.active = handler.hasPair() && this.container.getEnderPearls() >= cost;
            });
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Checkpoint title
        this.font.drawString(this.title.getFormattedText(), 8.0f, 6.0f, 0x404040);

        // Inventory title
        // this.ySize - 112: in the row straight above the inventory, + 2: vertically centre the text
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0f, (float)(this.ySize - 112 + 2), 0x404040);

        // Display amount of ender eyes needed
        if (!this.costText.isEmpty()) {
            int textWidth = this.minecraft.fontRenderer.getStringWidth(this.costText);
            float textLeft = (this.xSize * 2 / 3.0f) - (textWidth / 2.0f);
            float textTop = 20.0f;
            this.font.drawString(costText, textLeft + 1, textTop + 1, 0x203e08);
            this.font.drawString(costText, textLeft, textTop, 0x84ea2e);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);

        int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;
        this.blit(startX, startY, 0, 0, this.xSize, this.ySize);
    }
}
