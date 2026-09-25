package com.jhonswolf.fantasticpets;

import com.jhonswolf.fantasticpets.item.ModCreativeTabs;
import com.jhonswolf.fantasticpets.item.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

// Define a classe principal do mod e vincula ao ID
@Mod(FantasticPets.MOD_ID)
public class FantasticPets {

    // Armazena o ID principal do mod
    public static final String MOD_ID = "fantasticpets";

    // Construtor principal da classe
    public FantasticPets() {
        // Obtem o event bus do ciclo de vida do mod
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Registra os itens no jogo
        ModItems.register(modEventBus);


        // Registra a aba criativa no jogo
        ModCreativeTabs.register(modEventBus);

        // Inicializa o motor de animacoes GeckoLib
        GeckoLib.initialize();
    }
}