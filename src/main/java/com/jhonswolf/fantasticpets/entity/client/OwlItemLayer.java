package com.jhonswolf.fantasticpets.entity.client;

import com.jhonswolf.fantasticpets.entity.OwlEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

// Documentação: Camada responsável por desenhar o item sincronizado com o osso "item_target"
public class OwlItemLayer extends GeoRenderLayer<OwlEntity> {

    public OwlItemLayer(GeoRenderer<OwlEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, OwlEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        // 1. Obtém o item que a coruja está a carregar
        ItemStack pacote = animatable.getPacoteCarregado();

        // Cancela o desenho se a coruja não tiver encomendas
        if (pacote.isEmpty()) {
            return;
        }

        // 2. Procura o osso com o nome exato que está no Blockbench ("item_target")
        GeoBone bone = bakedModel.getBone("item_target").orElse(null);

        if (bone != null) {
            poseStack.pushPose();

            // 3. Sincroniza o sistema 3D com a posição e animação do osso nas patas
            RenderUtils.prepMatrixForBone(poseStack, bone);

            // 4. Ajustes finos de estética para o item não atravessar o chão
            // Move ligeiramente para baixo (eixo Y)
            poseStack.translate(0.0, -0.1, 0.0);

            // Roda o item 90 graus para que fique deitado, acompanhando a linha das patas
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));

            // Reduz o tamanho da carta para 70% do tamanho original para ser mais realista
            poseStack.scale(0.7f, 0.7f, 0.7f);

            // 5. Desenha o item no mundo
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    pacote,
                    ItemDisplayContext.GROUND,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    animatable.level(),
                    animatable.getId()
            );

            poseStack.popPose();
        }
    }
}