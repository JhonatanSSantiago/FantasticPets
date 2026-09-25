package com.jhonswolf.fantasticpets.block.custom;

import com.jhonswolf.fantasticpets.screen.ScribeDeskMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

// Define a Mesa de Caligrafia como um bloco que contém um Menu (Inventário)
public class ScribeDeskBlock extends Block {

    public ScribeDeskBlock(Properties pProperties) {
        super(pProperties);
    }

    // Quando o jogador clica na mesa
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            // Abre o menu no lado do Servidor usando o NetworkHooks do Forge
            NetworkHooks.openScreen((ServerPlayer) pPlayer, pState.getMenuProvider(pLevel, pPos), pPos);
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    // Fornece a ligação entre o Bloco e o Menu (ScribeDeskMenu)
    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return new SimpleMenuProvider((id, inventory, player) ->
                new ScribeDeskMenu(id, inventory, ContainerLevelAccess.create(pLevel, pPos)),
                Component.literal("Mesa de Caligrafia"));
    }
}