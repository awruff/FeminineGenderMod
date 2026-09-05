package com.feminine.gender.common;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public final class GenderArmor {

    public static final GenderArmor EMPTY = new GenderArmor(0F, 0F, false);

    private static final GenderArmor LEATHER = new GenderArmor(0.3F, 0.5F, true);
    private static final GenderArmor CHAINMAIL = new GenderArmor(0.5F, 0.2F, true);
    private static final GenderArmor GOLD = new GenderArmor(0.85F, 0F, true);
    private static final GenderArmor METAL = new GenderArmor(1F, 0F, true);
    private static final GenderArmor GENERIC = new GenderArmor(0.5F, 0F, true);

    private final float physicsResistance;
    private final float tightness;
    private final boolean coversBreasts;

    private GenderArmor(float physicsResistance, float tightness, boolean coversBreasts) {
        this.physicsResistance = physicsResistance;
        this.tightness = tightness;
        this.coversBreasts = coversBreasts;
    }

    public static GenderArmor forStack(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ArmorItem)) {
            return EMPTY;
        }
        ArmorItem armor = (ArmorItem) stack.getItem();
        if (armor.slot != 1) {

            return EMPTY;
        }
        switch (armor.getTier()) {
            case CLOTH:
                return LEATHER;
            case CHAIN:
                return CHAINMAIL;
            case GOLD:
                return GOLD;
            case IRON:
            case DIAMOND:
                return METAL;
            default:
                return GENERIC;
        }
    }

    public boolean coversBreasts() {
        return coversBreasts;
    }

    public float physicsResistance() {
        return physicsResistance;
    }

    public float tightness() {
        return tightness;
    }
}
