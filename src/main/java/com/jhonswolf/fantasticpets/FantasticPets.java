package com.jhonswolf.fantasticpets;

import com.jhonswolf.fantasticpets.block.ModBlocks;
import com.jhonswolf.fantasticpets.item.ModCreativeTabs;
import com.jhonswolf.fantasticpets.item.ModItems;
import com.jhonswolf.fantasticpets.network.ModMessages;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

// Classe principal que inicializa todos os registros do mod
@Mod(FantasticPets.MOD_ID)
public class FantasticPets {

    // Armazena o ID principal do mod
    public static final String MOD_ID = "fantasticpets";

    public FantasticPets() {
        // Obtem o event bus do ciclo de vida do mod
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Registra os blocos PRIMEIRO
        ModBlocks.register(modEventBus);

        // Registra os itens e a aba criativa
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Regista os Menus
        com.jhonswolf.fantasticpets.screen.ModMenuTypes.register(modEventBus);

        // INICIALIZA A REDE (Isto previne o NullPointerException)
        ModMessages.register();

        // Inicializa o motor de animacoes GeckoLib
        GeckoLib.initialize();
    }
}