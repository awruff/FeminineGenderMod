package com.feminine.gender.client;

import java.util.UUID;

import com.feminine.gender.common.GenderCache;
import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.config.GenderConfig;

import net.minecraft.client.Minecraft;

public final class ClientGenderState {

    private static boolean needsSync;
    private static UUID localUuid;

    private ClientGenderState() {
    }

    public static PlayerGenderData local() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return null;
        }
        UUID uuid = minecraft.player.getUuid();
        PlayerGenderData data = GenderCache.getOrCreate(uuid);
        if (!uuid.equals(localUuid)) {

            localUuid = uuid;
            GenderConfig.INSTANCE.readAppearance(data.settings());
            needsSync = true;
        }
        return data;
    }

    public static void save() {
        PlayerGenderData data = local();
        if (data == null) {
            return;
        }
        GenderConfig.INSTANCE.writeAppearance(data.settings());
        needsSync = true;
    }

    public static void tickSync() {

        PlayerGenderData data = local();
        if (data == null || !needsSync) {
            return;
        }
        GenderSettings settings = data.settings();
        if (ClientGenderNetworking.sendToServer(settings)) {
            needsSync = false;
        }
    }

    public static void markForSync() {
        needsSync = true;
    }

    public static void onDisconnect() {
        GenderCache.clear();
        localUuid = null;
        needsSync = false;
    }
}
