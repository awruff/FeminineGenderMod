package com.feminine.gender.client;

import java.io.IOException;
import java.util.UUID;

import com.feminine.gender.common.GenderCache;
import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.network.GenderNetworking;

import net.minecraft.client.Minecraft;

import net.ornithemc.osl.networking.api.PacketBuffer;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;
import net.ornithemc.osl.networking.api.client.ClientPacketListener;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;

public final class ClientGenderNetworking {

    private ClientGenderNetworking() {
    }

    public static void init() {
        ClientPlayNetworking.registerListener(GenderNetworking.CLIENTBOUND_SYNC, new ClientPacketListener.Buffer() {
            @Override
            public void handle(ClientPacketListener.Context ctx, PacketBuffer buffer) throws IOException {
                UUID uuid = buffer.readUuid();
                GenderSettings settings = new GenderSettings();
                if (!GenderNetworking.read(buffer, settings)) {
                    return;
                }
                Minecraft minecraft = ctx.minecraft();
                if (minecraft.player != null && uuid.equals(minecraft.player.getUuid())) {

                    return;
                }
                GenderCache.getOrCreate(uuid).settings().copyFrom(settings);
            }
        });

        ClientConnectionEvents.DISCONNECT.register(ctx -> ClientGenderState.onDisconnect());
    }

    public static boolean sendToServer(final GenderSettings settings) {
        if (!ClientPlayNetworking.isPlayReady(GenderNetworking.SERVERBOUND_SYNC)) {
            return false;
        }
        ClientPlayNetworking.send(GenderNetworking.SERVERBOUND_SYNC, buffer -> GenderNetworking.write(buffer, settings));
        return true;
    }
}
