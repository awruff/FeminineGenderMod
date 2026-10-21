package com.feminine.gender.client.cloud;

import com.feminine.gender.common.Gender;
import com.feminine.gender.common.GenderSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public final class CloudProfile {

    private CloudProfile() {
    }

    public static JsonObject toJson(GenderSettings settings) {
        JsonObject json = new JsonObject();
        json.addProperty("gender", settings.getGender().getSaveName());

        JsonArray offset = new JsonArray();
        offset.add(new JsonPrimitive(settings.getXOffset()));
        offset.add(new JsonPrimitive(settings.getYOffset()));
        offset.add(new JsonPrimitive(settings.getZOffset()));

        JsonObject physics = new JsonObject();
        physics.addProperty("enabled", settings.hasPhysics());
        physics.addProperty("uniboob", settings.isUniboob());
        physics.addProperty("bounce_multiplier", settings.getBounceMultiplier());
        physics.addProperty("floppiness", settings.getFloppiness());

        JsonObject breasts = new JsonObject();
        breasts.add("offset", offset);
        breasts.addProperty("size", settings.getBustSize());
        breasts.addProperty("cleavage", settings.getCleavage());
        breasts.add("physics", physics);
        json.add("breasts", breasts);

        JsonObject sound = new JsonObject();
        sound.addProperty("override_hurt", settings.hasHurtSounds());
        sound.addProperty("voice_pitch", settings.getVoicePitch());
        json.add("sound", sound);

        json.addProperty("show_in_armor", settings.showInArmor());
        return json;
    }

    public static GenderSettings fromJson(JsonObject json) {
        GenderSettings settings = new GenderSettings();
        settings.setGender(readGender(json.get("gender")));

        if (!readBreasts(json.get("breasts"), settings)) {
            settings.setXOffset(readFloat(json, "breasts_xOffset", 0F));
            settings.setYOffset(readFloat(json, "breasts_yOffset", 0F));
            settings.setZOffset(readFloat(json, "breasts_zOffset", 0F));
            settings.setBustSize(readFloat(json, "bust_size", GenderSettings.DEFAULT_BUST_SIZE));
            settings.setCleavage(readFloat(json, "breasts_cleavage", 0F));
            settings.setPhysics(readBoolean(json, "breast_physics", true));
            settings.setUniboob(readBoolean(json, "breasts_uniboob", true));
            settings.setBounceMultiplier(readFloat(json, "bounce_multiplier", GenderSettings.DEFAULT_BOUNCE));
            settings.setFloppiness(readFloat(json, "floppy_multiplier", GenderSettings.DEFAULT_FLOPPINESS));
        }

        if (!readSound(json.get("sound"), settings)) {
            settings.setHurtSounds(readBoolean(json, "hurt_sounds", true));
            settings.setVoicePitch(readFloat(json, "voice_pitch", 1F));
        }

        settings.setShowInArmor(readBoolean(json, "show_in_armor", true));
        return settings;
    }

    private static Gender readGender(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return Gender.MALE;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return Gender.byId(primitive.getAsInt());
        }
        return Gender.bySaveName(primitive.getAsString());
    }

    private static boolean readBreasts(JsonElement element, GenderSettings settings) {
        if (element == null || !element.isJsonObject()) {
            return false;
        }
        JsonObject breasts = element.getAsJsonObject();
        JsonElement offsetElement = breasts.get("offset");
        JsonElement physicsElement = breasts.get("physics");
        if (offsetElement == null || !offsetElement.isJsonArray() || offsetElement.getAsJsonArray().size() != 3
            || physicsElement == null || !physicsElement.isJsonObject()) {
            return false;
        }
        JsonArray offset = offsetElement.getAsJsonArray();
        JsonObject physics = physicsElement.getAsJsonObject();
        if (!isNumber(offset.get(0)) || !isNumber(offset.get(1)) || !isNumber(offset.get(2))
            || !isNumber(breasts.get("size")) || !isNumber(breasts.get("cleavage"))
            || !isBoolean(physics.get("enabled")) || !isBoolean(physics.get("uniboob"))
            || !isNumber(physics.get("bounce_multiplier")) || !isNumber(physics.get("floppiness"))) {
            return false;
        }

        settings.setXOffset(offset.get(0).getAsFloat());
        settings.setYOffset(offset.get(1).getAsFloat());
        settings.setZOffset(offset.get(2).getAsFloat());
        settings.setBustSize(breasts.get("size").getAsFloat());
        settings.setCleavage(breasts.get("cleavage").getAsFloat());
        settings.setPhysics(physics.get("enabled").getAsBoolean());
        settings.setUniboob(physics.get("uniboob").getAsBoolean());
        settings.setBounceMultiplier(physics.get("bounce_multiplier").getAsFloat());
        settings.setFloppiness(physics.get("floppiness").getAsFloat());
        return true;
    }

    private static boolean readSound(JsonElement element, GenderSettings settings) {
        if (element == null || !element.isJsonObject()) {
            return false;
        }
        JsonObject sound = element.getAsJsonObject();
        if (!isBoolean(sound.get("override_hurt")) || !isNumber(sound.get("voice_pitch"))) {
            return false;
        }
        settings.setHurtSounds(sound.get("override_hurt").getAsBoolean());
        settings.setVoicePitch(sound.get("voice_pitch").getAsFloat());
        return true;
    }

    private static float readFloat(JsonObject json, String key, float defaultValue) {
        JsonElement element = json.get(key);
        return isNumber(element) ? element.getAsFloat() : defaultValue;
    }

    private static boolean readBoolean(JsonObject json, String key, boolean defaultValue) {
        JsonElement element = json.get(key);
        return isBoolean(element) ? element.getAsBoolean() : defaultValue;
    }

    private static boolean isNumber(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }

    private static boolean isBoolean(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
    }
}
