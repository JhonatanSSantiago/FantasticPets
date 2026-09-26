package com.jhonswolf.fantasticpets;

import com.jhonswolf.fantasticpets.block.ModBlocks;
import com.jhonswolf.fantasticpets.entity.ModEntities;
import com.jhonswolf.fantasticpets.item.ModCreativeTabs;
import com.jhonswolf.fantasticpets.item.ModItems;
import com.jhonswolf.fantasticpets.network.ModMessages;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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

        // Registra o método commonSetup para correr durante a inicialização do mod
        modEventBus.addListener(this::commonSetup);

        // Registra os blocos PRIMEIRO
        ModBlocks.register(modEventBus);

        // Registra os itens e a aba criativa
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Registra os Menus
        com.jhonswolf.fantasticpets.screen.ModMenuTypes.register(modEventBus);

        // INICIALIZA A REDE (Isto previne o NullPointerException)
        ModMessages.register();

        com.jhonswolf.fantasticpets.entity.ModEntities.register(modEventBus);

        // Inicializa o motor de animacoes GeckoLib
        GeckoLib.initialize();
    }
    // Documentação: Método executado na fase de configuração comum do Forge
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Registra as regras de posicionamento natural da coruja no mundo
            SpawnPlacements.register(ModEntities.OWL.get(),
                    SpawnPlacements.Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Animal::checkAnimalSpawnRules);
        });
    }
}