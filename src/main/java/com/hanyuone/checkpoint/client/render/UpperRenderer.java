package com.hanyuone.checkpoint.client.render;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.tileentity.UpperTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.util.concurrent.atomic.AtomicReference;

public class UpperRenderer extends TileEntityRenderer<UpperTileEntity> {
    private final ResourceLocation ENDER_EYE_TEXTURE = new ResourceLocation("checkpoint:textures/models/ender_eye.png");
    private final EnderEyeModel eyeModel = new EnderEyeModel();

    int degrees;

    public UpperRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    private String formatBlockPos(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    private void renderName(UpperTileEntity tileEntityIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        EntityRendererManager manager = Minecraft.getInstance().getRenderManager();

        BlockPos tileEntityPos = tileEntityIn.getPos();
        double d0 = manager.getDistanceToCamera(tileEntityPos.getX(), tileEntityPos.getY(), tileEntityPos.getZ());

        RayTraceResult result = Minecraft.getInstance().objectMouseOver;
        if (result == null) return;

        BlockPos position = new BlockPos(result.getHitVec());

        // Code mostly adapted from the `EntityRenderer` version but with
        // optimisations since we aren't dealing with weird-shaped entities
        if (!(d0 > 4096.0D) && position.equals(tileEntityPos)) {
            tileEntityIn.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
                String displayNameIn;
                int colour;

                // We're guaranteed that the handler either has a pair
                // or is currently in the process of being paired
                if (handler.hasPair()) {
                    String formatted = this.formatBlockPos(handler.getBlockPos());
                    displayNameIn = I18n.format("hover.paired", formatted);
                    colour = 0x8055FF55;
                } else {
                    displayNameIn = I18n.format("hover.pairing_mode");
                    colour = 0x80FFFF55;
                }

                // Nametag generating magic
                matrixStackIn.push();
                matrixStackIn.translate(0.5D, 1.5D, 0.5D);
                matrixStackIn.rotate(manager.getCameraOrientation());
                matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();

                float f1 = Minecraft.getInstance().gameSettings.getTextBackgroundOpacity(0.25F);
                int j = (int)(f1 * 255.0F) << 24;

                FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
                float f2 = (float)(-fontRenderer.getStringWidth(displayNameIn) / 2);
                fontRenderer.renderString(displayNameIn, f2, 0, colour, false, matrix4f, bufferIn, false, j, packedLightIn);

                matrixStackIn.pop();
            });
        }
    }

    @Override
    public void render(UpperTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        AtomicReference<Boolean> hasName = new AtomicReference<>(true);

        matrixStackIn.push();
        matrixStackIn.translate(0.5, -0.5, 0.5);

        tileEntityIn.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            if (!handler.hasPair()) {
                if (handler.isIdEmpty()) {
                    // Mini-optimisation - just so we don't have to run through `renderName`
                    // and go through a capability if we're not generating a name at all
                    hasName.set(false);
                }

                return;
            }

            float currentTime = tileEntityIn.getWorld().getGameTime() + partialTicks;
            matrixStackIn.translate(0, (Math.sin(Math.PI * currentTime / 32) / 10), 0);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(degrees++ / 2.0f));
        });

        IVertexBuilder vertexBuilder = bufferIn.getBuffer(eyeModel.getRenderType(ENDER_EYE_TEXTURE));
        eyeModel.render(matrixStackIn, vertexBuilder, combinedLightIn, combinedOverlayIn, 1.0f, 1.0f, 1.0f, 1.0f);

        matrixStackIn.pop();

        if (hasName.get()) {
            this.renderName(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn);
        }
    }
}
