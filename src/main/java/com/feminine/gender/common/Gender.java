package com.feminine.gender.common;

public enum Gender {

    FEMALE("female", "\u00a7d", true),
    MALE("male", "\u00a79", false),
    OTHER("other", "\u00a7a", true);

    private static final Gender[] VALUES = values();

    private final String saveName;
    private final String color;
    private final boolean canHaveBreasts;

    Gender(String saveName, String color, boolean canHaveBreasts) {
        this.saveName = saveName;
        this.color = color;
        this.canHaveBreasts = canHaveBreasts;
    }

    public static Gender byId(int id) {
        return VALUES[Math.floorMod(id, VALUES.length)];
    }

    public static Gender bySaveName(String name) {
        for (Gender gender : VALUES) {
            if (gender.saveName.equals(name)) {
                return gender;
            }
        }
        return MALE;
    }

    public String getSaveName() {
        return saveName;
    }

    public String getColor() {
        return color;
    }

    public boolean canHaveBreasts() {
        return canHaveBreasts;
    }

    public Gender next() {
        switch (this) {
            case MALE:
                return FEMALE;
            case FEMALE:
                return OTHER;
            default:
                return MALE;
        }
    }

    public String getTranslationKey() {
        return "feminine_gender_mod.label." + saveName;
    }
}
