package com.feminine.gender.config;

import java.util.function.Predicate;

import com.feminine.gender.FeminineGenderMod;
import com.feminine.gender.client.cloud.SyncVerbosity;
import com.feminine.gender.common.Gender;
import com.feminine.gender.common.GenderSettings;

import net.ornithemc.osl.config.api.ConfigManager;
import net.ornithemc.osl.config.api.ConfigScope;
import net.ornithemc.osl.config.api.LoadingPhase;
import net.ornithemc.osl.config.api.config.BaseConfig;
import net.ornithemc.osl.config.api.config.option.BaseOption;
import net.ornithemc.osl.config.api.config.option.BooleanOption;
import net.ornithemc.osl.config.api.config.option.FloatOption;
import net.ornithemc.osl.config.api.config.option.StringOption;
import net.ornithemc.osl.config.api.serdes.FileSerializerType;
import net.ornithemc.osl.config.api.serdes.SerializerTypes;

public class GenderConfig extends BaseConfig {

    public static final GenderConfig INSTANCE = new GenderConfig();

    private static final String GROUP_APPEARANCE = "appearance";
    private static final String GROUP_CLIENT = "client";
    private static final String GROUP_CLOUD_SYNC = "cloud_sync";

    public final StringOption gender = new StringOption("gender", "Gender: female, male or other", Gender.MALE.getSaveName(), validGender());
    public final FloatOption bustSize = floatOption("bust_size", "Breast size", GenderSettings.DEFAULT_BUST_SIZE, GenderSettings.MIN_BUST_SIZE, GenderSettings.MAX_BUST_SIZE);
    public final FloatOption offsetX = floatOption("offset_x", "Separation", 0F, -1F, 1F);
    public final FloatOption offsetY = floatOption("offset_y", "Height", 0F, -1F, 1F);
    public final FloatOption offsetZ = floatOption("offset_z", "Depth", 0F, -1F, 0F);
    public final FloatOption cleavage = floatOption("cleavage", "Outward rotation", 0F, GenderSettings.MIN_CLEAVAGE, GenderSettings.MAX_CLEAVAGE);
    public final BooleanOption physics = new BooleanOption("physics", "Enable breast physics", true);
    public final BooleanOption uniboob = new BooleanOption("uniboob", "Use a single physics simulation for both breasts", true);
    public final FloatOption bounce = floatOption("bounce_multiplier", "Physics intensity", GenderSettings.DEFAULT_BOUNCE, GenderSettings.MIN_BOUNCE, GenderSettings.MAX_BOUNCE);
    public final FloatOption floppiness = floatOption("floppiness", "Physics momentum", GenderSettings.DEFAULT_FLOPPINESS, GenderSettings.MIN_FLOPPINESS, GenderSettings.MAX_FLOPPINESS);
    public final BooleanOption hurtSounds = new BooleanOption("hurt_sounds", "Play female hurt sounds", true);
    public final FloatOption voicePitch = floatOption("voice_pitch", "Hurt sound pitch", 1F, GenderSettings.MIN_VOICE_PITCH, GenderSettings.MAX_VOICE_PITCH);
    public final BooleanOption showInArmor = new BooleanOption("show_in_armor", "Render breasts while wearing a chestplate", true);
    public final BooleanOption appearanceSaved = new BooleanOption("appearance_saved", "Whether the appearance above has been saved locally; if not, it is loaded from the cloud", false);

    public final BooleanOption armorPhysicsOverride = new BooleanOption("armor_physics_override", "Ignore the physics resistance of worn armor", false);
    public final BooleanOption disableRendering = new BooleanOption("disable_rendering", "Disable all breast rendering", false);
    public final BooleanOption disableSoundReplacement = new BooleanOption("disable_sound_replacement", "Never replace other players' hurt sounds", false);
    public final BooleanOption firstTimeLoad = new BooleanOption("first_time_load", "Should the first time setup screen be displayed", true);

    public final BooleanOption cloudSync = new BooleanOption("cloud_sync", "Should settings be synced with the cloud", false);
    public final BooleanOption automaticCloudSync = new BooleanOption("sync_player_data", "Should setting updates be automatically synced to the cloud. Has no effect if cloud_sync is false", false);
    public final StringOption cloudServer = new StringOption("cloud_server", "The cloud sync server to connect to, leave empty for the default", "");
    public final StringOption syncLogVerbosity = new StringOption("sync_log_verbosity", "How verbose the cloud sync log should be: default or show_fetches", SyncVerbosity.DEFAULT.getSaveName(), validVerbosity());

    private boolean loaded;

    private GenderConfig() {
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean armorPhysicsOverride() {
        return loaded && armorPhysicsOverride.get();
    }

    public boolean disableRendering() {
        return loaded && disableRendering.get();
    }

    public boolean disableSoundReplacement() {
        return loaded && disableSoundReplacement.get();
    }

    public boolean firstTimeLoad() {
        return loaded && firstTimeLoad.get();
    }

    public boolean cloudSyncEnabled() {
        return loaded && cloudSync.get();
    }

    public boolean automaticCloudSync() {
        return loaded && automaticCloudSync.get();
    }

    public String cloudServer() {
        return loaded ? cloudServer.get() : "";
    }

    public SyncVerbosity syncLogVerbosity() {
        return loaded ? SyncVerbosity.bySaveName(syncLogVerbosity.get()) : SyncVerbosity.DEFAULT;
    }

    public boolean hasLocalAppearance() {
        if (!loaded) {
            return false;
        }
        if (appearanceSaved.get()) {
            return true;
        }
        BaseOption<?>[] appearance = {gender, bustSize, offsetX, offsetY, offsetZ, cleavage,
            physics, uniboob, bounce, floppiness, hurtSounds, voicePitch, showInArmor};
        for (BaseOption<?> option : appearance) {
            if (!option.isDefault()) {
                return true;
            }
        }
        return false;
    }

    private static FloatOption floatOption(String name, String description, float defaultValue, final float min, final float max) {
        return new FloatOption(name, description, defaultValue, new Predicate<Float>() {
            @Override
            public boolean test(Float value) {
                return value != null && !Float.isNaN(value) && value >= min && value <= max;
            }
        });
    }

    private static Predicate<String> validGender() {
        return new Predicate<String>() {
            @Override
            public boolean test(String value) {
                if (value == null) {
                    return false;
                }
                for (Gender gender : Gender.values()) {
                    if (gender.getSaveName().equals(value)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private static Predicate<String> validVerbosity() {
        return new Predicate<String>() {
            @Override
            public boolean test(String value) {
                if (value == null) {
                    return false;
                }
                for (SyncVerbosity verbosity : SyncVerbosity.values()) {
                    if (verbosity.getSaveName().equals(value)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static void register() {
        ConfigManager.register(INSTANCE);
    }

    @Override
    public String getNamespace() {
        return FeminineGenderMod.MOD_ID;
    }

    @Override
    public String getName() {
        return "Feminine Gender Mod";
    }

    @Override
    public String getSaveName() {
        return "settings.json";
    }

    @Override
    public ConfigScope getScope() {
        return ConfigScope.GLOBAL;
    }

    @Override
    public LoadingPhase getLoadingPhase() {
        return LoadingPhase.START;
    }

    @Override
    public FileSerializerType<?> getType() {
        return SerializerTypes.JSON;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void init() {
        registerOptions(GROUP_APPEARANCE, gender, bustSize, offsetX, offsetY, offsetZ, cleavage,
            physics, uniboob, bounce, floppiness, hurtSounds, voicePitch, showInArmor, appearanceSaved);
        registerOptions(GROUP_CLIENT, armorPhysicsOverride, disableRendering, disableSoundReplacement, firstTimeLoad);
        registerOptions(GROUP_CLOUD_SYNC, cloudSync, automaticCloudSync, cloudServer, syncLogVerbosity);
    }

    @Override
    public void load() {
        super.load();
        loaded = true;
    }

    @Override
    public void unload() {
        loaded = false;
        super.unload();
    }

    public void readAppearance(GenderSettings settings) {
        if (!loaded) {
            return;
        }
        settings.setGender(Gender.bySaveName(gender.get()));
        settings.setBustSize(bustSize.get());
        settings.setXOffset(offsetX.get());
        settings.setYOffset(offsetY.get());
        settings.setZOffset(offsetZ.get());
        settings.setCleavage(cleavage.get());
        settings.setPhysics(physics.get());
        settings.setUniboob(uniboob.get());
        settings.setBounceMultiplier(bounce.get());
        settings.setFloppiness(floppiness.get());
        settings.setHurtSounds(hurtSounds.get());
        settings.setVoicePitch(voicePitch.get());
        settings.setShowInArmor(showInArmor.get());
    }

    public void writeAppearance(GenderSettings settings) {
        if (!loaded) {
            return;
        }
        gender.set(settings.getGender().getSaveName());
        bustSize.set(settings.getBustSize());
        offsetX.set(settings.getXOffset());
        offsetY.set(settings.getYOffset());
        offsetZ.set(settings.getZOffset());
        cleavage.set(settings.getCleavage());
        physics.set(settings.hasPhysics());
        uniboob.set(settings.isUniboob());
        bounce.set(settings.getBounceMultiplier());
        floppiness.set(settings.getFloppiness());
        hurtSounds.set(settings.hasHurtSounds());
        voicePitch.set(settings.getVoicePitch());
        showInArmor.set(settings.showInArmor());
        appearanceSaved.set(true);
        save();
    }

    public void save() {
        ConfigManager.save(this);
    }
}
