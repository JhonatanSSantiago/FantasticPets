package com.jhonswolf.fantasticpets.event;

import com.jhonswolf.fantasticpets.FantasticPets;
import com.jhonswolf.fantasticpets.entity.ModEntities;
import com.jhonswolf.fantasticpets.entity.client.OwlRenderer;
import com.jhonswolf.fantasticpets.screen.ModMenuTypes;
import com.jhonswolf.fantasticpets.screen.ScribeDeskScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// Regista eventos que só acontecem no Cliente (Gráficos)
@Mod.EventBusSubscriber(modid = FantasticPets.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Liga o ScribeDeskMenu (Lógica) ao ScribeDeskScreen (Visual)
        MenuScreens.register(ModMenuTypes.SCRIBE_DESK_MENU.get(), ScribeDeskScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.OWL.get(),OwlRenderer::new);
    }

}