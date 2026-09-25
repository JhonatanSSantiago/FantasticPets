package com.jhonswolf.fantasticpets.item.custom;

import net.minecraft.world.item.Item;

// Classe base para TODOS os carimbos
public class WaxStampItem extends Item {

    // Variável que guarda a cor deste carimbo específico
    private final String stampColor;

    // Construtor atualizado: agora exige que definamos uma cor ao criar o item
    public WaxStampItem(String stampColor, Properties pProperties) {
        super(pProperties);
        this.stampColor = stampColor;
    }

    // Método para o Servidor perguntar a cor do carimbo
    public String getStampColor() {
        return this.stampColor;
    }
}