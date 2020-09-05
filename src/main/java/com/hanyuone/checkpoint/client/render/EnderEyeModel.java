package com.hanyuone.checkpoint.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class EnderEyeModel extends Model {
    private final ModelRenderer eye;

    public EnderEyeModel() {
        super(RenderType::getEntitySolid);
        textureWidth = 16;
        textureHeight = 16;

        eye = new ModelRenderer(this);
        eye.setRotationPoint(0.0F, 24.0F, 0.0F);
        eye.setTextureOffset(0, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, 0.0F, false);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        eye.render(matrixStack, buffer, packedLight, packedOverlay);
    }
}