package com.feminine.gender.common;

import java.util.UUID;

import com.feminine.gender.physics.BothBreastsPhysics;

public class PlayerGenderData {

    public final UUID uuid;
    private final GenderSettings settings = new GenderSettings();
    private final BothBreastsPhysics physics = new BothBreastsPhysics(this);

    public PlayerGenderData(UUID uuid) {
        this.uuid = uuid;
    }

    public GenderSettings settings() {
        return settings;
    }

    public BothBreastsPhysics physics() {
        return physics;
    }
}
