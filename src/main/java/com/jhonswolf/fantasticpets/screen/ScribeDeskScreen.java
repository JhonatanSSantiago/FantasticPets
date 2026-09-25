package com.jhonswolf.fantasticpets.screen;

import com.jhonswolf.fantasticpets.item.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScribeDeskScreen extends AbstractContainerScreen<ScribeDeskMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/dispenser.png");

    private MultiLineEditBox textBox;
    private EditBox recipientBox;
    private Button sealButton;
    private List<String> jogadoresOnline = new ArrayList<>();
    private String avisoErro = "";

    public ScribeDeskScreen(ScribeDeskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        // --- MÁGICA DO PAINEL DUPLO ---
        // Calcula a largura total: Inventário (176) + Espaço (5) + Pergaminho (240) = 421
        int larguraTotal = 421;

        // Empurra o inventário para a esquerda para centralizar o conjunto
        this.leftPos = (this.width - larguraTotal) / 2;

        // Define o ponto inicial do pergaminho logo à direita do inventário
        int pergaminhoX = this.leftPos + 181;
        // Sobe o pergaminho ligeiramente para alinhar visualmente com o inventário
        int pergaminhoY = this.topPos - 17;

        if (Minecraft.getInstance().getConnection() != null) {
            jogadoresOnline = Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                    .map(info -> info.getProfile().getName())
                    .collect(Collectors.toList());
        }

        // Caixa do Destinatário (Descemos para Y + 29 para alinhar com o texto "Para:")
        this.recipientBox = new EditBox(this.font, pergaminhoX + 40, pergaminhoY + 29, 185, 16, Component.literal("Destinatário"));
        this.recipientBox.setMaxLength(30);
        this.recipientBox.setBordered(false);
        this.recipientBox.setTextColor(0x333333);
        this.addRenderableWidget(this.recipientBox);

        // Caixa da Mensagem (Altura ajustada para 95 para não chocar com o contador de caracteres)
        this.textBox = new MultiLineEditBox(this.font, pergaminhoX + 10, pergaminhoY + 50, 220, 95, Component.literal("Texto"), Component.literal("Escreva aqui..."));
        this.textBox.setCharacterLimit(256);
        this.addRenderableWidget(this.textBox);

        // Botão Assinar e Selar (Posicionado corretamente no final do pergaminho)
        this.sealButton = this.addRenderableWidget(Button.builder(Component.literal("Assinar e Selar"), button -> {
            String destinatario = this.recipientBox.getValue().trim();
            if (destinatario.startsWith("@")) destinatario = destinatario.substring(1);

            if (destinatario.isEmpty()) {
                this.avisoErro = "Preencha o destinatario!";
                return;
            }

            if (!(this.menu.getSlot(1).getItem().getItem() instanceof com.jhonswolf.fantasticpets.item.custom.WaxStampItem)) {
                this.avisoErro = "Falta o Carimbo no slot!";
                return;
            }

            this.avisoErro = "";
            String sender = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getName().getString() : "Desconhecido";

            com.jhonswolf.fantasticpets.network.ModMessages.sendToServer(
                    new com.jhonswolf.fantasticpets.network.packet.SealScrollC2SPacket(sender, destinatario, this.textBox.getValue())
            );

            this.onClose();
        }).bounds(pergaminhoX + 60, pergaminhoY + 165, 120, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Desenha o inventário na nova posição (deslizado para a esquerda)
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        boolean hasPaper = this.menu.getSlot(0).getItem().is(Items.PAPER);

        this.textBox.visible = hasPaper;
        this.recipientBox.visible = hasPaper;
        this.sealButton.visible = hasPaper;

        if (hasPaper) {
            int pergaminhoX = this.leftPos + 181;
            int pergaminhoY = this.topPos - 17;
            int largura = 240;
            int altura = 200;

            // Fundo do Pergaminho
            guiGraphics.fill(pergaminhoX, pergaminhoY, pergaminhoX + largura, pergaminhoY + altura, 0xFFF5E4B5);
            guiGraphics.renderOutline(pergaminhoX, pergaminhoY, largura, altura, 0xFF333333);

            // Cabeçalho
            String playerName = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getName().getString() : "Desconhecido";
            guiGraphics.drawString(this.font, "De: " + playerName, pergaminhoX + 10, pergaminhoY + 10, 0x333333, false);
            guiGraphics.drawString(this.font, "Para:", pergaminhoX + 10, pergaminhoY + 29, 0x333333, false);
            guiGraphics.fill(pergaminhoX + 10, pergaminhoY + 45, pergaminhoX + largura - 10, pergaminhoY + 46, 0xFF999999);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (!this.avisoErro.isEmpty() && this.menu.getSlot(0).getItem().is(Items.PAPER)) {
            int pergaminhoX = this.leftPos + 181;
            int pergaminhoY = this.topPos - 17;
            guiGraphics.drawCenteredString(this.font, this.avisoErro, pergaminhoX + 120, pergaminhoY - 15, 0xFF0000);
        }

        // Renderiza a lista de jogadores online (Dropdown) com o novo alinhamento
        if (this.recipientBox != null && this.recipientBox.isFocused() && !jogadoresOnline.isEmpty() && this.recipientBox.visible) {

            // AJUSTE DE ALINHAMENTO: Move o fundo 4 píxeis para a esquerda e alarga-o em 8 píxeis
            int dropX = this.recipientBox.getX() - 4;
            int dropY = this.recipientBox.getY() + this.recipientBox.getHeight();
            int dropWidth = this.recipientBox.getWidth() + 8;
            int dropHeight = jogadoresOnline.size() * 12 + 4;

            guiGraphics.fill(dropX, dropY, dropX + dropWidth, dropY + dropHeight, 0xFF222222);
            guiGraphics.renderOutline(dropX, dropY, dropWidth, dropHeight, 0xFF555555);

            for (int i = 0; i < jogadoresOnline.size(); i++) {
                String nome = jogadoresOnline.get(i);
                int textY = dropY + 4 + (i * 12);
                int corTexto = 0xAAAAAA;

                if (mouseX >= dropX && mouseX <= dropX + dropWidth && mouseY >= textY && mouseY < textY + 12) {
                    guiGraphics.fill(dropX + 1, textY - 1, dropX + dropWidth - 1, textY + 11, 0xFF444444);
                    corTexto = 0xFFFFFF;
                }

                // Como recuámos o dropX em 4 píxeis, somar 4 aqui vai alinhar o texto exatamente com o de cima!
                guiGraphics.drawString(this.font, nome, dropX + 4, textY, corTexto, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipientBox != null && this.recipientBox.isFocused() && !jogadoresOnline.isEmpty() && button == 0 && this.recipientBox.visible) {

            // AJUSTE DE ALINHAMENTO: Devemos usar a mesma matemática aqui para os cliques funcionarem na área certa
            int dropX = this.recipientBox.getX() - 4;
            int dropY = this.recipientBox.getY() + this.recipientBox.getHeight();
            int dropWidth = this.recipientBox.getWidth() + 8;
            int dropHeight = jogadoresOnline.size() * 12 + 4;

            // Deteta se o clique do rato ocorreu dentro do novo quadrado da lista
            if (mouseX >= dropX && mouseX <= dropX + dropWidth && mouseY >= dropY && mouseY <= dropY + dropHeight) {
                int index = (int) ((mouseY - dropY - 4) / 12);
                if (index >= 0 && index < jogadoresOnline.size()) {
                    this.recipientBox.setValue(jogadoresOnline.get(index));
                    this.recipientBox.setFocused(false);
                    return true; // Bloqueia outros eventos de clique
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Impede que a tecla do inventário feche a tela enquanto estás a escrever
    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        // 1. Traduz o número da tecla (int) para um objeto 'Key' que o Minecraft entende
        InputConstants.Key teclaPressionada = InputConstants.getKey(pKeyCode, pScanCode);

        // 2. Compara a tecla convertida com o atalho configurado para o inventário (geralmente a letra 'E')
        if (this.minecraft.options.keyInventory.isActiveAndMatches(teclaPressionada)) {

            // 3. Se estivermos a escrever numa das caixas de texto
            if ((this.recipientBox != null && this.recipientBox.isFocused()) ||
                    (this.textBox != null && this.textBox.isFocused())) {

                // Envia a letra para o texto e bloqueia o fecho da tela
                return this.recipientBox.keyPressed(pKeyCode, pScanCode, pModifiers) ||
                        this.textBox.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
        }

        // Se não for a tecla do inventário, ou se não estivermos a escrever, mantém o comportamento normal
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}