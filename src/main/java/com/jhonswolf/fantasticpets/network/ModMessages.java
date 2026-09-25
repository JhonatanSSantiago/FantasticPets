package com.jhonswolf.fantasticpets.network;

import com.jhonswolf.fantasticpets.FantasticPets;
import com.jhonswolf.fantasticpets.network.packet.SealScrollC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

// Configura o canal de comunicacao do mod
public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(FantasticPets.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // Regista o pacote de selar o pergaminho
        net.messageBuilder(SealScrollC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SealScrollC2SPacket::new)
                .encoder(SealScrollC2SPacket::toBytes)
                .consumerMainThread(SealScrollC2SPacket::handle)
                .add();


    }

    // Metodo para enviar pacotes do Cliente para o Servidor
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}