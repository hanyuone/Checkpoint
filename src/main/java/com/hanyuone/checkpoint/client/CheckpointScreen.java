package com.hanyuone.checkpoint.client;

import com.hanyuone.checkpoint.capability.CheckpointPairProvider;
import com.hanyuone.checkpoint.container.CheckpointContainer;
import com.hanyuone.checkpoint.network.CheckpointPacketHandler;
import com.hanyuone.checkpoint.network.WarpPacket;
import com.hanyuone.checkpoint.tileentity.CheckpointTileEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CheckpointScreen extends ContainerScreen<CheckpointContainer> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("checkpoint:textures/gui/container/checkpoint.png");
    private Button warpButton;

    public CheckpointScreen(CheckpointContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init() {
        super.init();

        TileEntity containerEntity = this.container.getTileEntity();

        // Display button
        String warpButtonText = I18n.format("gui.checkpoint.warp");
        int buttonLeft = this.guiLeft + (this.xSize * 2 / 3) - (40 / 2);
        int buttonTop = this.guiTop + 32;

        this.warpButton = new Button(buttonLeft, buttonTop, 40, 20, warpButtonText, button -> {
            if (containerEntity instanceof CheckpointTileEntity) {
                CheckpointTileEntity checkpointEntity = (CheckpointTileEntity) containerEntity;

                // Assume hasPair() is always true, since the button is only active *if* hasPair() is true
                containerEntity.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
                    BlockPos pos = handler.getBlockPos();
                    WarpPacket packet = new WarpPacket(checkpointEntity.getPos(), pos);
                    CheckpointPacketHandler.INSTANCE.sendToServer(packet);
                });
            }
        });

        this.addButton(this.warpButton);
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

        // Warp cost text and warp button config
        CheckpointTileEntity checkpointEntity = (CheckpointTileEntity) this.container.getTileEntity();
        int cost = checkpointEntity.calculateCost();

        String costText;
        int topColour;
        int bottomColour;

        // `cost > 0` indicates that the cost is not -1 (meaning there's no pair) and not 0 (meaning
        // the paired portal is somehow in the exact same place as the original one), so the checkpoint has
        // a pair
        if (cost > 0) {
            costText = I18n.format("gui.checkpoint.cost", cost);
            topColour = 0x84ea2e;
            bottomColour = 0x203e08;

            // Checks whether we actually have enough ender pearls to transport us
            this.warpButton.active = checkpointEntity.getEnderPearls() >= cost;
        } else {
            costText = I18n.format("gui.checkpoint.notPaired");
            topColour = 0xe75558;
            bottomColour = 0x300703;

            this.warpButton.active = false;
        }

        // Configures a "layered text", similar to how anvils do it with "Enchantment cost"
        int textWidth = this.minecraft.fontRenderer.getStringWidth(costText);
        float textLeft = (this.xSize * 2 / 3.0f) - (textWidth / 2.0f);
        float textTop = 20.0f;

        this.font.drawString(costText, textLeft + 1, textTop + 1, bottomColour);
        this.font.drawString(costText, textLeft, textTop, topColour);
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
