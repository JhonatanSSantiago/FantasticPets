package com.jhonswolf.fantasticpets.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// Ecrã visual para leitura do pergaminho, adaptado do design original
public class ReadScrollScreen extends Screen {

    private final String sender;
    private final String recipient;
    private final String messageText;

    // Construtor: Recebe os dados extraídos do item quando a tela é aberta
    public ReadScrollScreen(String sender, String recipient, String messageText) {
        super(Component.literal("Lendo Pergaminho"));
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
    }

    @Override
    protected void init() {
        super.init();

        int largura = 240;
        int altura = 200;
        int x = (this.width - largura) / 2;
        int y = (this.height - altura) / 2;

        // Botão "X" no topo direito (pequeno quadrado 15x15)
        this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("X"), button -> {
            this.onClose();
        }).bounds(x + largura - 20, y + 5, 15, 15).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int largura = 240;
        int altura = 200;
        int x = (this.width - largura) / 2;
        int y = (this.height - altura) / 2;

        // Fundo cor de papel e borda
        guiGraphics.fill(x, y, x + largura, y + altura, 0xFFF5E4B5);
        guiGraphics.renderOutline(x, y, largura, altura, 0xFF333333);

        // Desenha o Cabeçalho usando as variáveis atualizadas
        guiGraphics.drawString(this.font, "De: " + sender, x + 10, y + 10, 0x333333, false);
        guiGraphics.drawString(this.font, "Para: " + recipient, x + 10, y + 25, 0x333333, false);

        // Desenha uma linha decorativa para separar o cabeçalho do texto
        guiGraphics.fill(x + 10, y + 40, x + largura - 10, y + 41, 0xFF888888);

        // Desenha a Mensagem com quebra automática de linha
        guiGraphics.drawWordWrap(this.font, Component.literal(messageText), x + 10, y + 48, largura - 20, 0x333333);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // Permite que entidades (como pássaros e outros jogadores) continuem a mover-se no fundo
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}