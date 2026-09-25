package com.jhonswolf.fantasticpets.item.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OpenedScrollItem extends Item {

    public OpenedScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // A interface de leitura é processada apenas no ecrã do jogador
        if (level.isClientSide()) {
            player.playSound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);

            CompoundTag nbt = stack.getTag();
            String autor = "Desconhecido";
            String destinatario = "Desconhecido";
            String mensagem = "";

            if (nbt != null) {
                // Lê as chaves atualizadas que guardámos na Mesa de Caligrafia
                if (nbt.contains("Sender")) autor = nbt.getString("Sender");
                if (nbt.contains("Recipient")) destinatario = nbt.getString("Recipient");
                if (nbt.contains("MessageText")) mensagem = nbt.getString("MessageText");
            }

            // Abre o ecrã de leitura (que implementámos ou vamos implementar a seguir)
            Minecraft.getInstance().setScreen(new com.jhonswolf.fantasticpets.client.ReadScrollScreen(autor, destinatario, mensagem));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        CompoundTag nbt = stack.getTag();

        if (nbt != null) {
            if (nbt.contains("Recipient")) {
                tooltip.add(Component.literal("§cPara: §f" + nbt.getString("Recipient")));
            }
            if (nbt.contains("Sender")) {
                tooltip.add(Component.literal("§7Assinado por: §e" + nbt.getString("Sender")));
            }
            if (nbt.contains("MessageText")) {
                tooltip.add(Component.literal("§8" + nbt.getString("MessageText")));
            }
        }
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
}