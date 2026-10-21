package com.feminine.gender.client.cloud;

public enum SyncVerbosity {

    DEFAULT("default"),
    SHOW_FETCHES("show_fetches");

    private final String saveName;

    SyncVerbosity(String saveName) {
        this.saveName = saveName;
    }

    public static SyncVerbosity bySaveName(String name) {
        for (SyncVerbosity verbosity : values()) {
            if (verbosity.saveName.equals(name)) {
                return verbosity;
            }
        }
        return DEFAULT;
    }

    public String getSaveName() {
        return saveName;
    }

    public String getTranslationKey() {
        return "feminine_gender_mod.sync_log.verbosity." + saveName;
    }
}
