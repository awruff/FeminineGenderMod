package com.feminine.gender.client.cloud;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;

final class CloudAuth {

    static final Duration AUTH_INVALIDATION_ADJUSTMENT = Duration.ofSeconds(30);

    private final String token;
    private final UUID account;
    private final Instant expires;

    private CloudAuth(String token, UUID account, Instant expires) {
        this.token = token;
        this.account = account;
        this.expires = expires;
    }

    static CloudAuth fromJson(JsonObject json) {
        return new CloudAuth(json.get("token").getAsString(), UUID.fromString(json.get("account").getAsString()),
            parseInstant(json.get("expires").getAsString()));
    }

    private static Instant parseInstant(String value) {
        try {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value, Instant::from);
        } catch (DateTimeParseException e) {
            return Instant.parse(value);
        }
    }

    String token() {
        return token;
    }

    UUID account() {
        return account;
    }

    Instant expires() {
        return expires;
    }

    boolean isExpired() {
        return expires.minus(AUTH_INVALIDATION_ADJUSTMENT).isBefore(Instant.now());
    }

    boolean isInvalidForClientPlayer() {
        Minecraft client = Minecraft.getInstance();
        return client.player == null || !account.equals(client.player.getUuid());
    }
}
