package com.feminine.gender.client.cloud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.feminine.gender.FeminineGenderMod;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.config.GenderConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Session;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;

public final class CloudSync {

    private static final String DEFAULT_CLOUD_URL = "https://wfgm.celestialfault.dev/v2";
    private static final Duration SYNC_COOLDOWN = Duration.ofSeconds(10);
    private static final int TIMEOUT_MILLIS = 5000;
    private static final int MAX_BATCH_SIZE = 20;

    private static Instant lastSync = Instant.EPOCH;
    private static final List<Instant> fetchErrors = new ArrayList<Instant>();
    private static volatile Instant disableFetchingUntil;
    private static volatile CloudAuth auth;

    private static final Object AUTH_LOCK = new Object();
    private static final Object SYNC_LOCK = new Object();
    private static final Executor EXECUTOR = createExecutor(FeminineGenderMod.MOD_ID + "$cloudSync");

    private static final String USER_AGENT = "FeminineGenderMod/" + getModVersion(FeminineGenderMod.MOD_ID).split("\\+")[0]
        + " Minecraft/" + getModVersion("minecraft")
        + " Fabric/" + getModVersion("fabricloader");

    private static final Queue<QueuedFetch> QUEUED = new ConcurrentLinkedDeque<QueuedFetch>();
    private static final Cache<UUID, Optional<JsonObject>> FETCH_CACHE = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES).concurrencyLevel(6).build();

    public static Executor createExecutor(final String name) {
        final AtomicInteger count = new AtomicInteger();
        return Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, name + "-" + count.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    private static String getModVersion(String modId) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        return mod.isPresent() ? mod.get().getMetadata().getVersion().getFriendlyString() : "unknown";
    }

    public static boolean syncOnCooldown() {
        return lastSync.plus(SYNC_COOLDOWN).isAfter(Instant.now());
    }

    public static SyncUnavailable unavailableReason() {
        Minecraft client = Minecraft.getInstance();
        UUID sessionUuid;
        try {
            GameProfile profile = client.getSession().getProfile();
            sessionUuid = profile == null ? null : profile.getId();
        } catch (IllegalArgumentException e) {
            sessionUuid = null;
        }
        // offline mode sessions use version 3 UUIDs
        if (sessionUuid == null || sessionUuid.version() != 4) {
            return SyncUnavailable.INVALID_ACCOUNT;
        }

        if (CloudUtils.hasTheSessionServiceBeenTamperedWith()) {
            return SyncUnavailable.INVALID_ACCOUNT;
        }

        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) {
            return null;
        }

        if (!client.isIntegratedServerRunning() && !networkHandler.getConnection().isEncrypted()) {
            return SyncUnavailable.OFFLINE_SERVER;
        }

        return null;
    }

    public static boolean isAvailable() {
        return unavailableReason() == null;
    }

    public static boolean isEnabled() {
        return isAvailable() && GenderConfig.INSTANCE.cloudSyncEnabled();
    }

    public static String getCloudServer() {
        String url = GenderConfig.INSTANCE.cloudServer();
        return url.trim().isEmpty() ? DEFAULT_CLOUD_URL : url;
    }

    private static void markFetchError() {
        synchronized (fetchErrors) {
            final Instant now = Instant.now();
            fetchErrors.add(now);
            Iterator<Instant> iterator = fetchErrors.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().plus(30, ChronoUnit.SECONDS).isBefore(now)) {
                    iterator.remove();
                }
            }
            if (fetchErrors.size() >= 5) {
                FeminineGenderMod.LOGGER.error("Too many recent sync errors, disabling future lookups for 5 minutes");
                disableFetchingUntil = now.plus(5, ChronoUnit.MINUTES);
            }
        }
    }

    private static boolean isFetchingDisabled(boolean ignoreConfig) {
        if (!isAvailable()) {
            return true;
        }
        if (!ignoreConfig && !isEnabled()) {
            return true;
        }
        Instant until = disableFetchingUntil;
        return until != null && until.isAfter(Instant.now());
    }

    private static Response request(String method, String url, String body, String authToken) {
        FeminineGenderMod.LOGGER.debug("Connecting to {}", url);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/json");
            if (authToken != null) {
                connection.setRequestProperty("Auth-Token", authToken);
            }
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                OutputStream out = connection.getOutputStream();
                try {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                } finally {
                    out.close();
                }
            }
            int status = connection.getResponseCode();
            InputStream in = status >= HttpURLConnection.HTTP_BAD_REQUEST ? connection.getErrorStream() : connection.getInputStream();
            return new Response(status, readFully(in));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readFully(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            in.close();
        }
    }

    private static JsonObject parseObject(String body) {
        return new JsonParser().parse(body).getAsJsonObject();
    }

    private static String generateServerId() {
        // https://github.com/hibiii/Kappa/blob/main/src/main/java/hibiii/kappa/Provider.java#L40-L42
        BigInteger intA = new BigInteger(128, new Random());
        BigInteger intB = new BigInteger(128, new Random(System.identityHashCode(new Object())));
        return intA.xor(intB).toString(16);
    }

    private static String getAuthToken() {
        synchronized (AUTH_LOCK) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                SyncLog.add("feminine_gender_mod.sync_log.authentication_failed");
                throw new IllegalStateException("Cannot get a new auth token while the client player is unset");
            }
            CloudAuth current = auth;
            if (current == null || current.isExpired() || current.isInvalidForClientPlayer()) {
                FeminineGenderMod.LOGGER.info("Authenticating with Mojang session servers");
                SyncLog.add("feminine_gender_mod.sync_log.authenticating_mojang");

                String serverId = generateServerId();
                Session session = client.getSession();

                try {
                    client.getSessionService().joinServer(session.getProfile(), session.getAccessToken(), serverId);
                } catch (AuthenticationException e) {
                    SyncLog.add("feminine_gender_mod.sync_log.authentication_failed");
                    throw new RuntimeException(e);
                }

                FeminineGenderMod.LOGGER.info("Obtaining new authentication token from the cloud sync server");
                SyncLog.add("feminine_gender_mod.sync_log.authenticating_sync");
                JsonObject body = new JsonObject();
                body.addProperty("server_id", serverId);
                body.addProperty("username", session.getUsername());

                Response response = request("POST", getCloudServer() + "/auth", body.toString(), null);
                if (response.status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    SyncLog.add("feminine_gender_mod.sync_log.authentication_failed");
                    throw new RuntimeException("Failed to authenticate with sync server: " + response.body);
                }

                current = CloudAuth.fromJson(parseObject(response.body));
                auth = current;
                if (current.isInvalidForClientPlayer()) {
                    FeminineGenderMod.LOGGER.warn("Authenticated account {} does not match the current player ({}); you likely have a misbehaving account switcher mod installed!", current.account(), client.player.getUuid());
                }
                FeminineGenderMod.LOGGER.info("Obtained authentication token for {}, expiry {}", current.account(), current.expires());
            }
            return current.token();
        }
    }

    public static CompletableFuture<Void> sync(PlayerGenderData data) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        synchronized (SYNC_LOCK) {
            if (syncOnCooldown()) {
                CompletableFuture<Void> future = new CompletableFuture<Void>();
                future.completeExceptionally(new SyncingTooFrequentlyException());
                return future;
            }
            lastSync = Instant.now();
        }

        return syncInternal(data, false);
    }

    private static CompletableFuture<Void> syncInternal(final PlayerGenderData data, final boolean resyncing) {
        final String json = CloudProfile.toJson(data.settings()).toString();
        return CompletableFuture.runAsync(() -> {
            String token = getAuthToken();

            SyncLog.add("feminine_gender_mod.sync_log.attempting_sync");

            Response response = request("PUT", getCloudServer() + "/player/" + data.uuid, json, token);
            if (response.status == HttpURLConnection.HTTP_UNAUTHORIZED && !resyncing) {
                SyncLog.add("feminine_gender_mod.sync_log.reauthenticating");
                FeminineGenderMod.LOGGER.warn("Auth token is invalid, attempting to reauth...");
                auth = null;
                syncInternal(data, true).join();
                return;
            } else if (response.status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new RuntimeException("Server responded " + response.status + ": " + response.body);
            }

            FeminineGenderMod.LOGGER.debug("Server responded to update: {}", response.body);
            SyncLog.add("feminine_gender_mod.sync_log.sync_success");
        }, EXECUTOR);
    }

    public static CompletableFuture<Void> deleteProfile(final PlayerGenderData data) {
        return CompletableFuture.runAsync(() -> {
            String token = getAuthToken();
            Response response = request("DELETE", getCloudServer() + "/player/" + data.uuid, null, token);
            if (response.status == HttpURLConnection.HTTP_NOT_FOUND) {
                SyncLog.add("feminine_gender_mod.sync_log.no_data_to_delete");
                return;
            } else if (response.status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                SyncLog.add("feminine_gender_mod.sync_log.data_deletion_failed");
                throw new RuntimeException("Server responded " + response.status + ": " + response.body);
            }
            FeminineGenderMod.LOGGER.debug("Deleted cloud sync profile");
            SyncLog.add("feminine_gender_mod.sync_log.data_deleted");
        }, EXECUTOR);
    }

    public static CompletableFuture<JsonObject> getProfile(UUID uuid) {
        return getProfile(uuid, false);
    }

    public static CompletableFuture<JsonObject> getProfile(final UUID uuid, boolean ignoreConfig) {
        if (isFetchingDisabled(ignoreConfig)) {
            return CompletableFuture.completedFuture(null);
        }
        if (uuid.version() != 4) {
            // some servers (hypixel, for one) give NPCs non-v4 UUIDs, which the sync server rejects with a 422
            return CompletableFuture.completedFuture(null);
        }
        Optional<JsonObject> cached = FETCH_CACHE.getIfPresent(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.orElse(null));
        }

        return CompletableFuture.supplyAsync(() -> {
            Response response = request("GET", getCloudServer() + "/player/" + uuid, null, null);
            if (response.status == HttpURLConnection.HTTP_NOT_FOUND || response.status == HttpURLConnection.HTTP_NO_CONTENT) {
                FeminineGenderMod.LOGGER.debug("Server replied no data for {}", uuid);
                FETCH_CACHE.put(uuid, Optional.<JsonObject>empty());
                return null;
            } else if (response.status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                markFetchError();
                throw new RuntimeException("Server responded " + response.status + ": " + response.body);
            }

            SyncLog.add("feminine_gender_mod.sync_log.get_single_profile", SyncVerbosity.SHOW_FETCHES);

            JsonObject data = parseObject(response.body);
            FETCH_CACHE.put(uuid, Optional.of(data));
            return data;
        }, EXECUTOR);
    }

    public static CompletableFuture<Map<UUID, JsonObject>> getMultiple(final Collection<UUID> uuids) {
        return CompletableFuture.supplyAsync(() -> {
            if (isFetchingDisabled(false)) {
                return Collections.<UUID, JsonObject>emptyMap();
            }

            JsonArray json = new JsonArray();
            for (UUID uuid : uuids) {
                json.add(new JsonPrimitive(uuid.toString()));
            }
            Response response = request("POST", getCloudServer() + "/players", json.toString(), null);
            if (response.status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                markFetchError();
                throw new RuntimeException("Server responded " + response.status + ": " + response.body);
            }

            SyncLog.add("feminine_gender_mod.sync_log.get_multiple_profiles", SyncVerbosity.SHOW_FETCHES);
            Map<UUID, JsonObject> data = new HashMap<UUID, JsonObject>();
            JsonElement users = parseObject(response.body).get("users");
            if (users != null && users.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : users.getAsJsonObject().entrySet()) {
                    if (entry.getValue().isJsonObject()) {
                        data.put(UUID.fromString(entry.getKey()), entry.getValue().getAsJsonObject());
                    }
                }
            }
            for (UUID uuid : uuids) {
                FETCH_CACHE.put(uuid, Optional.ofNullable(data.get(uuid)));
            }
            return data;
        }, EXECUTOR);
    }

    public static CompletableFuture<JsonObject> queueFetch(UUID uuid) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }

        Optional<JsonObject> cached = FETCH_CACHE.getIfPresent(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.orElse(null));
        }
        if (uuid.version() != 4) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<JsonObject> future = new CompletableFuture<JsonObject>();
        QUEUED.add(new QueuedFetch(uuid, future));
        return future;
    }

    public static void sendNextQueueBatch() {
        if (QUEUED.isEmpty()) {
            return;
        }

        final List<QueuedFetch> toFetch = new ArrayList<QueuedFetch>();
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            QueuedFetch next = QUEUED.poll();
            if (next == null) {
                break;
            }
            toFetch.add(next);
        }

        // send 3 or fewer queued players individually so their responses cache better
        if (toFetch.size() < 4) {
            FeminineGenderMod.LOGGER.debug("Sending {} queued sync queries", toFetch.size());
            for (final QueuedFetch queued : toFetch) {
                CompletableFuture.runAsync(() -> {
                    try {
                        queued.future.complete(getProfile(queued.uuid).join());
                    } catch (Exception e) {
                        queued.future.completeExceptionally(unwrap(e));
                    }
                });
            }
            return;
        }

        FeminineGenderMod.LOGGER.debug("Fetching sync data for {} players in bulk", toFetch.size());
        CompletableFuture.runAsync(() -> {
            List<UUID> uuids = new ArrayList<UUID>(toFetch.size());
            for (QueuedFetch queued : toFetch) {
                uuids.add(queued.uuid);
            }
            Map<UUID, JsonObject> result;
            try {
                result = getMultiple(uuids).join();
            } catch (Exception e) {
                Throwable actualException = unwrap(e);
                for (QueuedFetch queued : toFetch) {
                    queued.future.completeExceptionally(actualException);
                }
                return;
            }

            for (QueuedFetch queued : toFetch) {
                queued.future.complete(result.get(queued.uuid));
            }
        });
    }

    public static Throwable unwrap(Throwable e) {
        return e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;
    }

    private static final class QueuedFetch {

        final UUID uuid;
        final CompletableFuture<JsonObject> future;

        QueuedFetch(UUID uuid, CompletableFuture<JsonObject> future) {
            this.uuid = uuid;
            this.future = future;
        }
    }

    private static final class Response {

        final int status;
        final String body;

        Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}
