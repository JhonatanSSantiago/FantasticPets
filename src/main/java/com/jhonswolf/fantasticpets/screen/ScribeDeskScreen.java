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

// Documentação: Classe responsável por renderizar a interface gráfica unificada da Mesa de Caligrafia
public class ScribeDeskScreen extends AbstractContainerScreen<ScribeDeskMenu> {

    // Aponta para a imagem base criada no Figma (certifique-se de que o ficheiro tem 512x256 píxeis)
    // Usamos o formato contínuo "namespace:path" para evitar avisos de código obsoleto
    private static final ResourceLocation TEXTURE = new ResourceLocation("fantasticpets:textures/gui/scribe_desk_gui.png");

    private MultiLineEditBox textBox;
    private EditBox recipientBox;
    private Button sealButton;
    private List<String> jogadoresOnline = new ArrayList<>();
    private String avisoErro = "";

    public ScribeDeskScreen(ScribeDeskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        // Define o tamanho da área útil visível do design (A mesa de madeira + Pergaminho)
        this.imageWidth = 421;
        this.imageHeight = 210;
    }

    @Override
    protected void init() {
        super.init();

        // O Forge centraliza a tela automaticamente usando o imageWidth.
        // As coordenadas X e Y abaixo são relativas a esse centro.
        int pergaminhoX = this.leftPos + 181;
        int pergaminhoY = this.topPos + 15;

        // Carrega a lista de jogadores online para o menu dropdown
        if (Minecraft.getInstance().getConnection() != null) {
            jogadoresOnline = Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                    .map(info -> info.getProfile().getName())
                    .collect(Collectors.toList());
        }

        // 1. Caixa do Destinatário (Para:)
        this.recipientBox = new EditBox(this.font, pergaminhoX + 40, pergaminhoY + 29, 185, 16, Component.literal("Destinatário"));
        this.recipientBox.setMaxLength(30);
        this.recipientBox.setBordered(false); // Remove a borda padrão do Minecraft
        this.recipientBox.setTextColor(0x333333); // Cor do texto escura, como tinta
        this.addRenderableWidget(this.recipientBox);

        /// 2. Caixa da Mensagem Principal (Com fundo transparente simplificado)
        this.textBox = new MultiLineEditBox(this.font, pergaminhoX + 10, pergaminhoY + 50, 220, 95, Component.literal("Texto"), Component.literal("Escreva aqui...")) {
            @Override
            protected void renderBackground(GuiGraphics guiGraphics) {
                // Ao deixarmos este método completamente vazio, anulamos o comportamento padrão do Minecraft
                // O retângulo preto deixa de ser desenhado, revelando a tua arte do Figma por baixo!
            }
        };
        this.textBox.setCharacterLimit(256);
        this.addRenderableWidget(this.textBox);

        // 3. Botão Assinar e Selar
        this.sealButton = this.addRenderableWidget(Button.builder(Component.literal("Assinar e Selar"), button -> {
            String destinatario = this.recipientBox.getValue().trim();
            if (destinatario.startsWith("@")) destinatario = destinatario.substring(1);

            if (destinatario.isEmpty()) {
                this.avisoErro = "Preencha o destinatario!";
                return;
            }

            // Verifica se o carimbo está no Slot 1
            if (!(this.menu.getSlot(1).getItem().getItem() instanceof com.jhonswolf.fantasticpets.item.custom.WaxStampItem)) {
                this.avisoErro = "Falta o Carimbo no slot!";
                return;
            }

            this.avisoErro = "";
            String sender = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getName().getString() : "Desconhecido";

            // Envia os dados para o servidor selar o pergaminho
            com.jhonswolf.fantasticpets.network.ModMessages.sendToServer(
                    new com.jhonswolf.fantasticpets.network.packet.SealScrollC2SPacket(sender, destinatario, this.textBox.getValue())
            );

            this.onClose(); // Fecha a interface após selar
        }).bounds(pergaminhoX + 60, pergaminhoY + 155, 120, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Desenha a imagem base carregada do Figma (Lê 421x210 de um ficheiro 512x256)
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 512, 256);

        // Verifica se há papel no Slot 0 para mostrar os campos de escrita
        boolean hasPaper = this.menu.getSlot(0).getItem().is(Items.PAPER);

        this.textBox.visible = hasPaper;
        this.recipientBox.visible = hasPaper;
        this.sealButton.visible = hasPaper;

        if (hasPaper) {
            int pergaminhoX = this.leftPos + 181;
            int pergaminhoY = this.topPos + 15;
            int largura = 240;

            // Desenha os rótulos de texto estáticos
            String playerName = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getName().getString() : "Desconhecido";
            guiGraphics.drawString(this.font, "De: " + playerName, pergaminhoX + 10, pergaminhoY + 10, 0x333333, false);
            guiGraphics.drawString(this.font, "Para:", pergaminhoX + 10, pergaminhoY + 29, 0x333333, false);

            // Linha divisória de enfeite
            guiGraphics.fill(pergaminhoX + 10, pergaminhoY + 45, pergaminhoX + largura - 10, pergaminhoY + 46, 0xFF999999);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics); // Escurece o fundo fora da interface
        super.render(guiGraphics, mouseX, mouseY, delta); // Desenha slots e itens
        this.renderTooltip(guiGraphics, mouseX, mouseY); // Desenha as descrições (tooltips)

        // Exibe mensagem de erro (Ex: "Falta o Carimbo no slot!") se necessário
        if (!this.avisoErro.isEmpty() && this.menu.getSlot(0).getItem().is(Items.PAPER)) {
            int pergaminhoX = this.leftPos + 181;
            int pergaminhoY = this.topPos + 15;
            guiGraphics.drawCenteredString(this.font, this.avisoErro, pergaminhoX + 120, pergaminhoY - 15, 0xFF0000);
        }

        // Renderiza o Dropdown (lista suspensa) de jogadores
        if (this.recipientBox != null && this.recipientBox.isFocused() && !jogadoresOnline.isEmpty() && this.recipientBox.visible) {
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

                // Efeito visual ao passar o rato por cima do nome
                if (mouseX >= dropX && mouseX <= dropX + dropWidth && mouseY >= textY && mouseY < textY + 12) {
                    guiGraphics.fill(dropX + 1, textY - 1, dropX + dropWidth - 1, textY + 11, 0xFF444444);
                    corTexto = 0xFFFFFF;
                }

                guiGraphics.drawString(this.font, nome, dropX + 4, textY, corTexto, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Lógica para clicar e selecionar um jogador do Dropdown
        if (this.recipientBox != null && this.recipientBox.isFocused() && !jogadoresOnline.isEmpty() && button == 0 && this.recipientBox.visible) {
            int dropX = this.recipientBox.getX() - 4;
            int dropY = this.recipientBox.getY() + this.recipientBox.getHeight();
            int dropWidth = this.recipientBox.getWidth() + 8;
            int dropHeight = jogadoresOnline.size() * 12 + 4;

            if (mouseX >= dropX && mouseX <= dropX + dropWidth && mouseY >= dropY && mouseY <= dropY + dropHeight) {
                int index = (int) ((mouseY - dropY - 4) / 12);
                if (index >= 0 && index < jogadoresOnline.size()) {
                    this.recipientBox.setValue(jogadoresOnline.get(index));
                    this.recipientBox.setFocused(false);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        // Impede que o jogador feche acidentalmente a tela com a tecla do inventário enquanto digita
        InputConstants.Key teclaPressionada = InputConstants.getKey(pKeyCode, pScanCode);

        if (this.minecraft.options.keyInventory.isActiveAndMatches(teclaPressionada)) {
            if ((this.recipientBox != null && this.recipientBox.isFocused()) ||
                    (this.textBox != null && this.textBox.isFocused())) {
                return this.recipientBox.keyPressed(pKeyCode, pScanCode, pModifiers) ||
                        this.textBox.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}