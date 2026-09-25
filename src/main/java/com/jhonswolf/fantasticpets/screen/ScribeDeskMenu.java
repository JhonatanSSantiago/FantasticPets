package com.jhonswolf.fantasticpets.screen;

import com.jhonswolf.fantasticpets.block.ModBlocks;
import com.jhonswolf.fantasticpets.item.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// Controla a lógica dos itens e slots da Mesa de Caligrafia no Servidor
public class ScribeDeskMenu extends AbstractContainerMenu {

    // O "baú" temporário da nossa mesa (Tamanho 2: Papel e Carimbo)
    private final SimpleContainer deskInventory = new SimpleContainer(2);
    private final ContainerLevelAccess access;

    // Construtor chamado pelo Cliente
    public ScribeDeskMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, ContainerLevelAccess.NULL);
    }

    // Construtor principal chamado pelo Servidor
    public ScribeDeskMenu(int pContainerId, Inventory inv, ContainerLevelAccess access) {
        super(ModMenuTypes.SCRIBE_DESK_MENU.get(), pContainerId);
        this.access = access;

        // Adiciona os nossos 2 slots customizados na interface (Posições X e Y provisórias)
        // Slot 0: Papel
        this.addSlot(new Slot(deskInventory, 0, 15, 30));
        // Slot 1: Carimbo
        this.addSlot(new Slot(deskInventory, 1, 15, 50));

        // Adiciona os 36 slots padrão do inventário do jogador na parte inferior
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    // Verifica se o jogador ainda está perto da mesa
    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, ModBlocks.SCRIBE_DESK.get());
    }

    // O que acontece quando o jogador pressiona "ESC" ou fecha o ecrã
    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        // Devolve os itens que estavam na mesa (Papel e Carimbo) para o chão/inventário
        this.access.execute((level, pos) -> {
            this.clearContainer(pPlayer, this.deskInventory);
        });
    }

    // Métodos auxiliares para desenhar os slots do jogador (matemática padrão do Minecraft)
    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 100 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 158));
        }
    }

    // Este método é acionado sempre que o jogador faz Shift+Clique num slot
    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        // Obter o slot que foi clicado
        Slot sourceSlot = this.slots.get(index);

        // Se o slot estiver vazio, não fazemos nada
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        // Pega no item que está no slot clicado e cria uma cópia de segurança
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Variáveis matemáticas para identificar os slots do jogador (2 a 37)
        int PLAYER_INVENTORY_START = 2; // Onde começa o inventário do jogador
        int PLAYER_HOTBAR_END = 38;     // Onde termina a hotbar do jogador

        // Clicou no Slot 0 (Papel) ou Slot 1 (Carimbo) da Mesa -> Move para o inventário
        if (index == 0 || index == 1) {
            if (!this.moveItemStackTo(sourceStack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Clicou no inventário do jogador -> Tenta mover para a Mesa
        else if (index >= PLAYER_INVENTORY_START) {

            // Se o item for Papel, move para o Slot 0 da Mesa
            if (sourceStack.is(Items.PAPER)) {
                if (!this.moveItemStackTo(sourceStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Se o item for o Carimbo, move para o Slot 1 da Mesa
            else if (sourceStack.getItem() instanceof com.jhonswolf.fantasticpets.item.custom.WaxStampItem) {
                if (!this.moveItemStackTo(sourceStack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Se não for nem Papel nem Carimbo, move o item entre a Hotbar e o Inventário principal
            else if (index < 29) { // Está no inventário principal, move para a hotbar
                if (!this.moveItemStackTo(sourceStack, 29, PLAYER_HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 29 && index < PLAYER_HOTBAR_END) { // Está na hotbar, move para o inventário
                if (!this.moveItemStackTo(sourceStack, PLAYER_INVENTORY_START, 29, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        // Atualiza visualmente a quantidade de itens no slot clicado
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }
}