package com.feminine.gender.common;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class GenderCache {

    private static final Map<UUID, PlayerGenderData> CACHE = new ConcurrentHashMap<UUID, PlayerGenderData>();

    private GenderCache() {
    }

    public static PlayerGenderData getOrCreate(UUID uuid) {
        PlayerGenderData data = CACHE.get(uuid);
        if (data == null) {
            data = new PlayerGenderData(uuid);
            PlayerGenderData existing = CACHE.putIfAbsent(uuid, data);
            if (existing != null) {
                data = existing;
            }
        }
        return data;
    }

    public static PlayerGenderData get(UUID uuid) {
        return CACHE.get(uuid);
    }

    public static Collection<PlayerGenderData> all() {
        return CACHE.values();
    }

    public static void remove(UUID uuid) {
        CACHE.remove(uuid);
    }

    public static void removeIf(Predicate<PlayerGenderData> predicate) {
        CACHE.values().removeIf(predicate);
    }

    public static void clear() {
        CACHE.clear();
    }
}
