package com.feminine.gender.physics;

import com.feminine.gender.common.GenderArmor;
import com.feminine.gender.common.PlayerGenderData;

import net.minecraft.entity.living.LivingEntity;

public class BothBreastsPhysics {

    private final BreastPhysics left;
    private final BreastPhysics right;

    public BothBreastsPhysics(PlayerGenderData data) {
        this.left = new BreastPhysics(data);
        this.right = new BreastPhysics(data);
    }

    public BreastPhysics left() {
        return left;
    }

    public BreastPhysics right() {
        return right;
    }

    public void tick(LivingEntity entity) {
        GenderArmor armor = GenderArmor.forStack(entity.getArmor(2));
        left.update(entity, armor);
        right.update(entity, armor);
    }
}
