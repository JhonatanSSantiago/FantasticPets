package com.jhonswolf.fantasticpets.screen;

import com.jhonswolf.fantasticpets.FantasticPets;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Classe responsável por registar as interfaces com inventário do mod
public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, FantasticPets.MOD_ID);

    // Regista o Menu da Mesa de Caligrafia (A classe ScribeDeskMenu será criada a seguir)
    public static final RegistryObject<MenuType<ScribeDeskMenu>> SCRIBE_DESK_MENU =
            registerMenuType("scribe_desk_menu", ScribeDeskMenu::new);

    // Método auxiliar para criar e registar o MenuType
    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}