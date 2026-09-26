package com.jhonswolf.fantasticpets.entity.client;

import com.jhonswolf.fantasticpets.entity.OwlEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

// Classe renomeada para OwlRenderer
public class OwlRenderer extends GeoEntityRenderer<OwlEntity> {

    public OwlRenderer(EntityRendererProvider.Context renderManager) {
        // Usa o novo OwlModel
        super(renderManager, new OwlModel());
        this.shadowRadius = 0.4f;
        // ADICIONAR ESTA LINHA: Acopla a lógica do item à renderização da coruja
        this.addRenderLayer(new OwlItemLayer(this));
    }

    @Override
    public void render(OwlEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        } else {
            poseStack.scale(1.0f, 1.0f, 1.0f);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}