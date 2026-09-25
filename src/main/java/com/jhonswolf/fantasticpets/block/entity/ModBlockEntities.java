package com.jhonswolf.fantasticpets.block.entity;

import com.jhonswolf.fantasticpets.FantasticPets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

// Documentação: Classe responsável por registar blocos que possuem "memória" (BlockEntities).
// Necessário para guardar itens permanentemente, como em baús ou caixas de correio.
public class ModBlockEntities {

    // Cria o contentor de registos associado ao ID do nosso mod
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FantasticPets.MOD_ID);

    // O código para registar o seu futuro bloco decorativo de cartas entrará aqui!

    // Método para ligar este registo ao EventBus principal do Forge
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}