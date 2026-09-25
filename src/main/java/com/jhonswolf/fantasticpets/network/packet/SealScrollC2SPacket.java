package com.jhonswolf.fantasticpets.network.packet;

import com.jhonswolf.fantasticpets.item.ModItems;
import com.jhonswolf.fantasticpets.screen.ScribeDeskMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// Pacote enviado pelo Cliente para o Servidor quando o botão "Assinar e Selar" é clicado
public class SealScrollC2SPacket {
    private final String sender;
    private final String recipient;
    private final String messageText;

    // Construtor usado no Cliente
    public SealScrollC2SPacket(String sender, String recipient, String messageText) {
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
    }

    // Construtor usado no Servidor para descodificar a mensagem
    public SealScrollC2SPacket(FriendlyByteBuf buf) {
        this.sender = buf.readUtf();
        this.recipient = buf.readUtf();
        this.messageText = buf.readUtf();
    }

    // Codifica a mensagem para viajar pela rede
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(sender);
        buf.writeUtf(recipient);
        buf.writeUtf(messageText);
    }

    // O QUE ACONTECE NO SERVIDOR
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player != null) {
                // Verifica se o jogador tem o menu da Mesa de Caligrafia aberto
                if (player.containerMenu instanceof ScribeDeskMenu menu) {

                    // Acede diretamente aos slots da mesa
                    Slot paperSlot = menu.getSlot(0);
                    Slot stampSlot = menu.getSlot(1);

                    ItemStack paperStack = paperSlot.getItem();
                    ItemStack stampStack = stampSlot.getItem();

                    // Verifica se o papel é papel e se o carimbo pertence à família WaxStampItem
                    if (paperStack.is(Items.PAPER) && stampStack.getItem() instanceof com.jhonswolf.fantasticpets.item.custom.WaxStampItem) {

                        // 1. Transforma o item genérico no nosso Carimbo para podermos ler a cor
                        com.jhonswolf.fantasticpets.item.custom.WaxStampItem stampItem = (com.jhonswolf.fantasticpets.item.custom.WaxStampItem) stampStack.getItem();

                        // 2. Consome 1 Papel do slot da mesa
                        paperStack.shrink(1);

                        // 3. Aplica 1 de dano ao Carimbo no slot da mesa
                        stampStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

                        // 4. Cria o Pergaminho Selado e grava os dados (NBT)
                        ItemStack sealedScroll = new ItemStack(ModItems.SEALED_SCROLL.get());
                        CompoundTag nbt = sealedScroll.getOrCreateTag();
                        nbt.putString("Sender", this.sender);
                        nbt.putString("Recipient", this.recipient);
                        nbt.putString("MessageText", this.messageText);

                        // MAGIA ACONTECE AQUI: Copia a cor do carimbo usado e cola na carta!
                        nbt.putString("StampColor", stampItem.getStampColor());

                        // 5. Entrega a carta ao jogador
                        if (!player.getInventory().add(sealedScroll)) {
                            player.drop(sealedScroll, false);
                        }

                        // 6. Reproduz o som de selar
                        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        });

        // Confirma que o pacote foi processado com sucesso
        context.setPacketHandled(true);
        return true;
    }
}