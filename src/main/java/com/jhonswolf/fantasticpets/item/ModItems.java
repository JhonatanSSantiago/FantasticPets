package com.jhonswolf.fantasticpets.item;

import com.jhonswolf.fantasticpets.FantasticPets;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Registra e gerencia todos os itens do mod
public class ModItems {

    // Cria o registrador oficial de itens do mod
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FantasticPets.MOD_ID);

    // Registra o Pergaminho (Feito com papel e linha)
  //  public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll",
     //       () -> new Item(new Item.Properties()));

    // Registra a Cera Derretida (Feita na fornalha com favo de mel)
    public static final RegistryObject<Item> MELTED_WAX = ITEMS.register("melted_wax",
            () -> new Item(new Item.Properties()));

    // Registra o Carimbo de Selamento (Com durabilidade de 30 usos)
    // Atualiza o registo para informar que este é o carimbo vermelho
    public static final RegistryObject<Item> WAX_STAMP = ITEMS.register("wax_stamp",
            () -> new com.jhonswolf.fantasticpets.item.custom.WaxStampItem("red", new Item.Properties().defaultDurability(64)));

    // Registra o Pergaminho Selado usando a nova classe
    public static final RegistryObject<Item> SEALED_SCROLL = ITEMS.register("sealed_scroll",
            () -> new com.jhonswolf.fantasticpets.item.custom.SealedScrollItem(new Item.Properties()));

    // Registra o Pergaminho Aberto usando a nova classe (mantendo o limite de stack de 1)
    public static final RegistryObject<Item> OPENED_SCROLL = ITEMS.register("opened_scroll",
            () -> new com.jhonswolf.fantasticpets.item.custom.OpenedScrollItem(new Item.Properties().stacksTo(1)));

    // Registra a lista de itens no Event Bus principal
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}