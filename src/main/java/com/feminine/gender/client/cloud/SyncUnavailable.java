package com.feminine.gender.client.cloud;

public enum SyncUnavailable {

    INVALID_ACCOUNT("feminine_gender_mod.cloud.unavailable.invalid_account"),
    OFFLINE_SERVER("feminine_gender_mod.cloud.unavailable.offline_server");

    private final String translationKey;

    SyncUnavailable(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
