package com.jhonswolf.fantasticpets.entity.client;

import com.jhonswolf.fantasticpets.FantasticPets;
import com.jhonswolf.fantasticpets.entity.OwlEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

// Classe renomeada para OwlModel
public class OwlModel extends GeoModel<OwlEntity> {

    @Override
    public ResourceLocation getModelResource(OwlEntity animatable) {
        // Aponta para os ficheiros renomeados para "owl"
        return new ResourceLocation(FantasticPets.MOD_ID, "geo/owl.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(OwlEntity animatable) {
        return new ResourceLocation(FantasticPets.MOD_ID, "textures/entity/owl.png");
    }

    @Override
    public ResourceLocation getAnimationResource(OwlEntity animatable) {
        return new ResourceLocation(FantasticPets.MOD_ID, "animations/owl.animation.json");
    }
}