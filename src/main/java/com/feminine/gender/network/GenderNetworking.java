package com.feminine.gender.network;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.feminine.gender.FeminineGenderMod;
import com.feminine.gender.common.Gender;
import com.feminine.gender.common.GenderCache;
import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.common.PlayerGenderData;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;

import net.ornithemc.osl.core.api.util.NamespacedIdentifier;
import net.ornithemc.osl.networking.api.ChannelIdentifiers;
import net.ornithemc.osl.networking.api.ChannelRegistry;
import net.ornithemc.osl.networking.api.PacketBuffer;
import net.ornithemc.osl.networking.api.server.ServerConnectionEvents;
import net.ornithemc.osl.networking.api.server.ServerPacketListener;
import net.ornithemc.osl.networking.api.server.ServerPlayNetworking;

public final class GenderNetworking {

    static final byte PROTOCOL_VERSION = 1;

    public static final NamespacedIdentifier SERVERBOUND_SYNC = ChannelRegistry.register(
        ChannelIdentifiers.from(FeminineGenderMod.MOD_ID, "serverbound/sync"), false, true);
    public static final NamespacedIdentifier CLIENTBOUND_SYNC = ChannelRegistry.register(
        ChannelIdentifiers.from(FeminineGenderMod.MOD_ID, "clientbound/sync"), true, false);

    private GenderNetworking() {
    }

    public static void initServer() {
        ServerPlayNetworking.registerListener(SERVERBOUND_SYNC, new ServerPacketListener.Buffer() {
            @Override
            public void handle(ServerPacketListener.Context ctx, PacketBuffer buffer) throws IOException {
                ServerPlayerEntity sender = ctx.player();
                if (sender == null) {
                    return;
                }
                GenderSettings settings = new GenderSettings();
                if (!read(buffer, settings)) {
                    return;
                }
                UUID uuid = sender.getUuid();
                GenderCache.getOrCreate(uuid).settings().copyFrom(settings);
                broadcast(ctx.server().getPlayerManager().getAll(), uuid, settings, sender);
            }
        });

        ServerConnectionEvents.PLAY_READY.register(ctx -> {
            ServerPlayerEntity joining = ctx.player();
            if (joining == null) {
                return;
            }

            for (ServerPlayerEntity other : ctx.server().getPlayerManager().getAll()) {
                if (other == joining) {
                    continue;
                }
                PlayerGenderData data = GenderCache.get(other.getUuid());
                if (data != null) {
                    sendTo(joining, other.getUuid(), data.settings());
                }
            }
        });

        ServerConnectionEvents.DISCONNECT.register(ctx -> {
            ServerPlayerEntity player = ctx.player();
            if (player != null) {
                GenderCache.remove(player.getUuid());
            }
        });
    }

    private static void broadcast(List<ServerPlayerEntity> players, UUID uuid, GenderSettings settings, ServerPlayerEntity except) {
        for (ServerPlayerEntity player : players) {
            if (player != except) {
                sendTo(player, uuid, settings);
            }
        }
    }

    private static void sendTo(ServerPlayerEntity player, final UUID uuid, final GenderSettings settings) {
        if (!ServerPlayNetworking.isPlayReady(player, CLIENTBOUND_SYNC)) {
            return;
        }
        ServerPlayNetworking.send(player, CLIENTBOUND_SYNC, buffer -> {
            buffer.writeUuid(uuid);
            write(buffer, settings);
        });
    }

    public static void write(PacketBuffer buffer, GenderSettings settings) {
        buffer.writeByte(PROTOCOL_VERSION);
        buffer.writeByte(settings.getGender().ordinal());
        buffer.writeFloat(settings.getBustSize());
        buffer.writeFloat(settings.getXOffset());
        buffer.writeFloat(settings.getYOffset());
        buffer.writeFloat(settings.getZOffset());
        buffer.writeFloat(settings.getCleavage());
        buffer.writeBoolean(settings.hasPhysics());
        buffer.writeBoolean(settings.isUniboob());
        buffer.writeFloat(settings.getBounceMultiplier());
        buffer.writeFloat(settings.getFloppiness());
        buffer.writeBoolean(settings.hasHurtSounds());
        buffer.writeFloat(settings.getVoicePitch());
        buffer.writeBoolean(settings.showInArmor());
    }

    public static boolean read(PacketBuffer buffer, GenderSettings settings) {
        byte version = buffer.readByte();
        if (version != PROTOCOL_VERSION) {
            FeminineGenderMod.LOGGER.debug("Ignoring sync packet with unsupported version " + version);
            return false;
        }
        settings.setGender(Gender.byId(buffer.readByte()));
        settings.setBustSize(buffer.readFloat());
        settings.setXOffset(buffer.readFloat());
        settings.setYOffset(buffer.readFloat());
        settings.setZOffset(buffer.readFloat());
        settings.setCleavage(buffer.readFloat());
        settings.setPhysics(buffer.readBoolean());
        settings.setUniboob(buffer.readBoolean());
        settings.setBounceMultiplier(buffer.readFloat());
        settings.setFloppiness(buffer.readFloat());
        settings.setHurtSounds(buffer.readBoolean());
        settings.setVoicePitch(buffer.readFloat());
        settings.setShowInArmor(buffer.readBoolean());
        return true;
    }
}
