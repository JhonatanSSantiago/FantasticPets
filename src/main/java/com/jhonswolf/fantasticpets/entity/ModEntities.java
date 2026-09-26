package com.jhonswolf.fantasticpets.entity;

import com.jhonswolf.fantasticpets.FantasticPets;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Registo de todas as novas entidades (mobs) do mod
public class ModEntities {
    // Cria a lista (DeferredRegister) para guardar os tipos de entidade
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FantasticPets.MOD_ID);

    // Regista a espécie "Coruja Mensageira"
    // MobCategory.CREATURE significa que é um animal pacífico que faz spawn naturalmente
    public static final RegistryObject<EntityType<OwlEntity>> OWL =
            ENTITY_TYPES.register("owl", () -> EntityType.Builder.of(OwlEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.9f) // Tamanho da caixa de colisão (largura, altura) em blocos
                    .build("owl"));

    // Método para ligar este registo ao EventBus principal
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}