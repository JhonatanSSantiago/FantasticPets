package com.jhonswolf.fantasticpets.item;

import com.jhonswolf.fantasticpets.FantasticPets;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// Registra e gerencia as abas criativas do mod
public class ModCreativeTabs {

    // Cria o registrador de abas criativas
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FantasticPets.MOD_ID);

    // Cria a aba especifica do Fantastic Pets
    public static final RegistryObject<CreativeModeTab> FANTASTIC_PETS_TAB = CREATIVE_MODE_TABS.register("fantastic_pets_tab",
            () -> CreativeModeTab.builder()
                    // Define o novo icone da aba como sendo o Pergaminho Selado
                    .icon(() -> new ItemStack(ModItems.SEALED_SCROLL.get()))
                    // Define o nome interno de traducao da aba
                    .title(Component.translatable("creativetab.fantasticpets"))
                    // Define os itens que aparecerao na aba
                    .displayItems((pParameters, pOutput) -> {

                        // Adiciona a mesa na aba
                        pOutput.accept(com.jhonswolf.fantasticpets.block.ModBlocks.SCRIBE_DESK.get());

                        // Adiciona todos os nossos itens na aba do jogador
               //         pOutput.accept(ModItems.SCROLL.get());
                        pOutput.accept(ModItems.MELTED_WAX.get());
                        pOutput.accept(ModItems.WAX_STAMP.get());
                        pOutput.accept(ModItems.SEALED_SCROLL.get());
                        pOutput.accept(ModItems.OPENED_SCROLL.get());

                    })
                    .build());

    // Registra o DeferredRegister no Event Bus principal
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}