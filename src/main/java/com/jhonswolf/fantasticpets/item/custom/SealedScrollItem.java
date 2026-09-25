package com.jhonswolf.fantasticpets.item.custom;

import com.jhonswolf.fantasticpets.item.ModItems;
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

public class SealedScrollItem extends Item {

    public SealedScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // LADO DO SERVIDOR: Troca o item selado pelo pergaminho aberto
            ItemStack pergaminhoAberto = new ItemStack(ModItems.OPENED_SCROLL.get());

            // Copia a mensagem, o remetente e o destinatário para o novo item
            if (stack.hasTag()) {
                pergaminhoAberto.setTag(stack.getTag().copy());
            }

            // Remove o selado e entrega o aberto
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (stack.isEmpty()) {
                player.setItemInHand(hand, pergaminhoAberto);
            } else {
                if (!player.getInventory().add(pergaminhoAberto)) {
                    player.drop(pergaminhoAberto, false);
                }
            }
        } else {
            // LADO DO CLIENTE: Toca apenas o som de quebrar o selo (Opção A Pura)
            player.playSound(net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    // Mantém a sua lógica de exibir as informações no inventário
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
            // ADICIONAR ESTAS LINHAS: Mostra a cor do selo usado
            if (nbt.contains("StampColor")) {
                String cor = nbt.getString("StampColor");
                // Traduz a cor para um texto mais amigável apenas para visualização
                String nomeCor = cor.equals("red") ? "Vermelho" : cor;
                tooltip.add(Component.literal("§dCor do Selo: " + nomeCor));
            }
        }
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
}