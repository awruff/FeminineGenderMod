package com.feminine.gender.common;

public class GenderSettings {

    public static final float MIN_BUST_SIZE = 0F;
    public static final float MAX_BUST_SIZE = 0.8F;
    public static final float DEFAULT_BUST_SIZE = 0.6F;

    public static final float MIN_CLEAVAGE = 0F;
    public static final float MAX_CLEAVAGE = 0.1F;

    public static final float MIN_BOUNCE = 0F;
    public static final float MAX_BOUNCE = 0.5F;
    public static final float DEFAULT_BOUNCE = 0.333F;

    public static final float MIN_FLOPPINESS = 0.25F;
    public static final float MAX_FLOPPINESS = 1F;
    public static final float DEFAULT_FLOPPINESS = 0.75F;

    public static final float MIN_VOICE_PITCH = 0.8F;
    public static final float MAX_VOICE_PITCH = 1.2F;

    private Gender gender = Gender.MALE;
    private float bustSize = DEFAULT_BUST_SIZE;
    private float xOffset = 0F;
    private float yOffset = 0F;
    private float zOffset = 0F;
    private float cleavage = 0F;

    private boolean physicsEnabled = true;
    private boolean uniboob = true;
    private float bounceMultiplier = DEFAULT_BOUNCE;
    private float floppiness = DEFAULT_FLOPPINESS;

    private boolean hurtSounds = true;
    private float voicePitch = 1F;
    private boolean showInArmor = true;

    public static float clamp(float value, float min, float max) {
        if (Float.isNaN(value)) {
            return min;
        }
        return value < min ? min : (value > max ? max : value);
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender == null ? Gender.MALE : gender;
    }

    public float getBustSize() {
        return bustSize;
    }

    public void setBustSize(float bustSize) {
        this.bustSize = clamp(bustSize, MIN_BUST_SIZE, MAX_BUST_SIZE);
    }

    public float getXOffset() {
        return xOffset;
    }

    public void setXOffset(float xOffset) {
        this.xOffset = clamp(xOffset, -1F, 1F);
    }

    public float getYOffset() {
        return yOffset;
    }

    public void setYOffset(float yOffset) {
        this.yOffset = clamp(yOffset, -1F, 1F);
    }

    public float getZOffset() {
        return zOffset;
    }

    public void setZOffset(float zOffset) {
        this.zOffset = clamp(zOffset, -1F, 0F);
    }

    public float getCleavage() {
        return cleavage;
    }

    public void setCleavage(float cleavage) {
        this.cleavage = clamp(cleavage, MIN_CLEAVAGE, MAX_CLEAVAGE);
    }

    public boolean hasPhysics() {
        return physicsEnabled;
    }

    public void setPhysics(boolean physicsEnabled) {
        this.physicsEnabled = physicsEnabled;
    }

    public boolean isUniboob() {
        return uniboob;
    }

    public void setUniboob(boolean uniboob) {
        this.uniboob = uniboob;
    }

    public float getBounceMultiplier() {
        return bounceMultiplier;
    }

    public void setBounceMultiplier(float bounceMultiplier) {
        this.bounceMultiplier = clamp(bounceMultiplier, MIN_BOUNCE, MAX_BOUNCE);
    }

    public float getFloppiness() {
        return floppiness;
    }

    public void setFloppiness(float floppiness) {
        this.floppiness = clamp(floppiness, MIN_FLOPPINESS, MAX_FLOPPINESS);
    }

    public boolean hasHurtSounds() {
        return hurtSounds;
    }

    public void setHurtSounds(boolean hurtSounds) {
        this.hurtSounds = hurtSounds;
    }

    public float getVoicePitch() {
        return voicePitch;
    }

    public void setVoicePitch(float voicePitch) {
        this.voicePitch = clamp(voicePitch, MIN_VOICE_PITCH, MAX_VOICE_PITCH);
    }

    public boolean showInArmor() {
        return showInArmor;
    }

    public void setShowInArmor(boolean showInArmor) {
        this.showInArmor = showInArmor;
    }

    public void copyFrom(GenderSettings other) {
        this.gender = other.gender;
        this.bustSize = other.bustSize;
        this.xOffset = other.xOffset;
        this.yOffset = other.yOffset;
        this.zOffset = other.zOffset;
        this.cleavage = other.cleavage;
        this.physicsEnabled = other.physicsEnabled;
        this.uniboob = other.uniboob;
        this.bounceMultiplier = other.bounceMultiplier;
        this.floppiness = other.floppiness;
        this.hurtSounds = other.hurtSounds;
        this.voicePitch = other.voicePitch;
        this.showInArmor = other.showInArmor;
    }
}
