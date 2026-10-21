package com.feminine.gender.client.cloud;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.feminine.gender.config.GenderConfig;

import net.minecraft.util.math.MathHelper;

public final class SyncLog {

    private static final int MAX_ENTRIES = 6;
    private static final List<Entry> SYNC_LOG = new ArrayList<Entry>();

    private SyncLog() {
    }

    public static SyncVerbosity verbosity() {
        return GenderConfig.INSTANCE.syncLogVerbosity();
    }

    public static void add(String translationKey, SyncVerbosity verbosity) {
        if (verbosity().ordinal() >= verbosity.ordinal()) {
            add(translationKey);
        }
    }

    public static void add(String translationKey) {
        synchronized (SYNC_LOG) {
            SYNC_LOG.add(new Entry(translationKey, Instant.now()));
            if (SYNC_LOG.size() > MAX_ENTRIES) {
                SYNC_LOG.remove(0);
            }
        }
    }

    public static List<Entry> entries() {
        synchronized (SYNC_LOG) {
            return new ArrayList<Entry>(SYNC_LOG);
        }
    }

    public static final class Entry {

        public static final int NEW_COLOR = 0xFF55FF55;
        public static final int OLD_COLOR = 0xFF34A100;

        private final String translationKey;
        private final Instant timestamp;

        Entry(String translationKey, Instant timestamp) {
            this.translationKey = translationKey;
            this.timestamp = timestamp;
        }

        public String translationKey() {
            return translationKey;
        }

        public int color() {
            long secondsPassed = Instant.now().getEpochSecond() - timestamp.getEpochSecond();
            float delta = MathHelper.clamp(secondsPassed / 60F, 0F, 1F);
            return lerpChannel(delta, NEW_COLOR, OLD_COLOR, 24) | lerpChannel(delta, NEW_COLOR, OLD_COLOR, 16)
                | lerpChannel(delta, NEW_COLOR, OLD_COLOR, 8) | lerpChannel(delta, NEW_COLOR, OLD_COLOR, 0);
        }

        private static int lerpChannel(float delta, int from, int to, int shift) {
            int a = (from >>> shift) & 0xFF;
            int b = (to >>> shift) & 0xFF;
            return ((int) (a + delta * (b - a)) & 0xFF) << shift;
        }
    }
}
