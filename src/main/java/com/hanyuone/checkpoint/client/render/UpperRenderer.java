package com.hanyuone.checkpoint.client.render;

import com.hanyuone.checkpoint.capability.checkpoint.CheckpointPairProvider;
import com.hanyuone.checkpoint.tileentity.UpperTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;

public class UpperRenderer extends TileEntityRenderer<UpperTileEntity> {
    private final ResourceLocation ENDER_EYE_TEXTURE = new ResourceLocation("checkpoint:textures/models/ender_eye.png");
    private final EnderEyeModel eyeModel = new EnderEyeModel();

    int degrees;

    public UpperRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(UpperTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        matrixStackIn.translate(0.5, -0.5, 0.5);

        tileEntityIn.getCapability(CheckpointPairProvider.CHECKPOINT_PAIR, null).ifPresent(handler -> {
            if (!handler.hasPair()) return;

            float currentTime = tileEntityIn.getWorld().getGameTime() + partialTicks;
            matrixStackIn.translate(0, (Math.sin(Math.PI * currentTime / 32) / 10), 0);
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(degrees++ / 2.0f));
        });

        IVertexBuilder vertexBuilder = bufferIn.getBuffer(eyeModel.getRenderType(ENDER_EYE_TEXTURE));
        eyeModel.render(matrixStackIn, vertexBuilder, combinedLightIn, combinedOverlayIn, 1.0f, 1.0f, 1.0f, 1.0f);

        matrixStackIn.pop();
    }
}
