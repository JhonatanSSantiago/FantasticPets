package com.jhonswolf.fantasticpets.event;

import com.jhonswolf.fantasticpets.FantasticPets;
import com.jhonswolf.fantasticpets.entity.OwlEntity;
import com.jhonswolf.fantasticpets.entity.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Ouve os eventos vitais que ocorrem quando o Minecraft está a iniciar (EventBus.MOD)
@Mod.EventBusSubscriber(modid = FantasticPets.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    // Associa a vida e a velocidade (atributos) à nossa Coruja
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(ModEntities.OWL.get(), OwlEntity.createAttributes().build());
    }
}