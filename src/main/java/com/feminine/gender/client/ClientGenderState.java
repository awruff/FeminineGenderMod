package com.feminine.gender.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.feminine.gender.FeminineGenderMod;
import com.feminine.gender.client.cloud.CloudProfile;
import com.feminine.gender.client.cloud.CloudSync;
import com.feminine.gender.client.cloud.SyncLog;
import com.feminine.gender.client.gui.BaseGenderScreen;
import com.feminine.gender.common.GenderCache;
import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.common.SyncStatus;
import com.feminine.gender.config.GenderConfig;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;

public final class ClientGenderState {

    private static final long CACHE_EXPIRY_MILLIS = TimeUnit.MINUTES.toMillis(15);
    private static final Executor MAIN_THREAD = new Executor() {
        @Override
        public void execute(Runnable task) {
            Minecraft.getInstance().executeTask(task);
        }
    };

    private static boolean needsSync;
    private static int timer;

    private ClientGenderState() {
    }

    public static PlayerGenderData local() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return null;
        }
        return getOrLoad(minecraft.player.getUuid());
    }

    public static PlayerGenderData getOrLoad(UUID uuid) {
        PlayerGenderData data = GenderCache.get(uuid);
        if (data == null) {
            data = GenderCache.getOrCreate(uuid);
            loadGenderInfo(data, true, false);
        }
        data.lastAccessed = System.currentTimeMillis();
        return data;
    }

    private static boolean isLocal(UUID uuid) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && uuid.equals(minecraft.player.getUuid());
    }

    public static CompletableFuture<PlayerGenderData> loadGenderInfo(final PlayerGenderData data, final boolean markForSync, boolean bypassQueue) {
        final boolean local = isLocal(data.uuid);
        if (local && GenderConfig.INSTANCE.hasLocalAppearance()) {
            GenderConfig.INSTANCE.readAppearance(data.settings());
            data.syncStatus = SyncStatus.CACHED;
            if (markForSync) {
                needsSync = true;
            }
            return CompletableFuture.completedFuture(data);
        }
        if (data.syncStatus != SyncStatus.UNKNOWN) {
            return CompletableFuture.completedFuture(data);
        }

        CompletableFuture<JsonObject> future = bypassQueue ? CloudSync.getProfile(data.uuid) : CloudSync.queueFetch(data.uuid);
        return future.handleAsync((json, error) -> {
            if (error != null) {
                Throwable cause = CloudSync.unwrap(error);
                FeminineGenderMod.LOGGER.error("Failed to fetch profile from sync server", cause);
                throw new CompletionException(cause);
            }
            // the server may have sent this player's data while the fetch was in flight
            if (json != null && data.syncStatus == SyncStatus.UNKNOWN) {
                GenderSettings settings = CloudProfile.fromJson(json);
                data.updateFromSync(settings);
                if (markForSync && local) {
                    needsSync = true;
                }
            }
            return data;
        }, MAIN_THREAD);
    }

    public static void save() {
        PlayerGenderData data = local();
        if (data == null) {
            return;
        }
        GenderConfig.INSTANCE.writeAppearance(data.settings());
        needsSync = true;
        data.needsCloudSync = true;
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.world == null || minecraft.player == null) {
            return;
        }
        timer++;

        PlayerGenderData data = local();
        if (needsSync && ClientGenderNetworking.sendToServer(data.settings())) {
            needsSync = false;
        }

        if (timer % 40 == 0) {
            CloudSync.sendNextQueueBatch();
            attemptCloudSync(minecraft, data);
            expireUnusedEntries(data);
        }
    }

    private static void attemptCloudSync(Minecraft minecraft, final PlayerGenderData data) {
        if (!data.needsCloudSync) {
            return;
        }
        if (minecraft.screen instanceof BaseGenderScreen) {
            return;
        }
        if (!GenderConfig.INSTANCE.automaticCloudSync()) {
            return;
        }
        if (CloudSync.syncOnCooldown()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                CloudSync.sync(data).join();
                FeminineGenderMod.LOGGER.info("Synced player data to the cloud");
            } catch (Exception e) {
                FeminineGenderMod.LOGGER.error("Failed to sync player data", CloudSync.unwrap(e));
                SyncLog.add("feminine_gender_mod.sync_log.failed_to_sync_data");
            }
        });
        data.needsCloudSync = false;
    }

    private static void expireUnusedEntries(final PlayerGenderData local) {
        final long cutoff = System.currentTimeMillis() - CACHE_EXPIRY_MILLIS;
        GenderCache.removeIf(data -> data != local && data.syncStatus != SyncStatus.SYNCED && data.lastAccessed < cutoff);
    }

    public static void onDisconnect() {
        GenderCache.clear();
        needsSync = false;
    }
}
