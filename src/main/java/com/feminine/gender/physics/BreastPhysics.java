package com.feminine.gender.physics;

import java.util.Random;

import com.feminine.gender.common.GenderArmor;
import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.config.GenderConfig;

import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class BreastPhysics {

    public static final float TIGHTNESS_REDUCTION_FACTOR = 0.15F;

    private static final Random RANDOM = new Random();

    private float bounceVelX = 0, targetBounceX = 0, velocityX = 0, positionX, prePositionX;

    private float bounceVel = 0, targetBounceY = 0, velocity = 0, positionY, prePositionY;

    private float bounceRotVel = 0, targetRotVel = 0, rotVelocity = 0, bounceRotation, preBounceRotation;

    private float breastSize = 0, preBreastSize = 0;

    private boolean wasSneaking;
    private boolean wasSleeping;
    private boolean hasPrePos;
    private double preX, preY, preZ;

    private final PlayerGenderData data;
    private int randomB = 1;
    private double lastVerticalMoveVelocity;

    public BreastPhysics(PlayerGenderData data) {
        this.data = data;
    }

    private GenderSettings settings() {
        return data.settings();
    }

    private static float calcRotation(LivingEntity entity, float bounceIntensity) {
        Entity vehicle = entity.vehicle;
        if (vehicle instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) vehicle;
            return -((living.bodyYaw - living.lastBodyYaw) / 15F) * bounceIntensity;
        } else if (vehicle != null) {
            return -((vehicle.yaw - vehicle.lastYaw) / 15F) * bounceIntensity;
        }
        return -((entity.bodyYaw - entity.lastBodyYaw) / 15F) * bounceIntensity;
    }

    public void update(LivingEntity entity, GenderArmor armor) {
        boolean armorPhysicsOverride = GenderConfig.INSTANCE.armorPhysicsOverride();

        this.prePositionY = this.positionY;
        this.prePositionX = this.positionX;
        this.preBounceRotation = this.bounceRotation;
        this.preBreastSize = this.breastSize;

        if (!this.hasPrePos) {
            this.preX = entity.x;
            this.preY = entity.y;
            this.preZ = entity.z;
            this.hasPrePos = true;
            return;
        }

        float targetBreastSize = settings().getBustSize();
        float breastWeight = targetBreastSize * 1.25F;

        if (!settings().getGender().canHaveBreasts()) {
            targetBreastSize = 0;
        } else {
            float tightness = armorPhysicsOverride ? 0F : MathHelper.clamp(armor.tightness(), 0F, 1F);

            targetBreastSize *= 1 - TIGHTNESS_REDUCTION_FACTOR * tightness;
        }

        breastSize += (breastSize < targetBreastSize)
            ? Math.abs(breastSize - targetBreastSize) / 2F
            : -Math.abs(breastSize - targetBreastSize) / 2F;

        double motionY = entity.y - this.preY;
        this.preX = entity.x;
        this.preY = entity.y;
        this.preZ = entity.z;

        float bounceIntensity = targetBreastSize * 3F * Math.round(settings().getBounceMultiplier() * 3 * 100) / 100F;
        float resistance = armorPhysicsOverride ? 0F : MathHelper.clamp(armor.physicsResistance(), 0F, 1F);

        bounceIntensity *= 1 - resistance;

        if (!settings().isUniboob()) {
            bounceIntensity *= MathHelper.nextFloat(RANDOM, 0.5F, 2.5F);
        }

        tickMovement(entity, motionY, bounceIntensity);
        tickPose(entity, bounceIntensity);
        tickVehicle(entity, bounceIntensity, breastWeight);
        tickArmSwing(entity, bounceIntensity);
        finishTick();
    }

    public void updateSizeOnly(GenderArmor armor) {
        if (settings().getGender().canHaveBreasts()) {
            float size = settings().getBustSize();
            if (!GenderConfig.INSTANCE.armorPhysicsOverride()) {
                float tightness = MathHelper.clamp(armor.tightness(), 0F, 1F);
                size *= 1 - TIGHTNESS_REDUCTION_FACTOR * tightness;
            }
            this.breastSize = size;
            this.preBreastSize = size;
        } else {
            this.breastSize = 0F;
            this.preBreastSize = 0F;
        }
    }

    private void tickMovement(LivingEntity entity, double motionY, float bounceIntensity) {
        double vertVelocity = entity.velocityY;

        if ((lastVerticalMoveVelocity <= 0 && vertVelocity > 0) || (lastVerticalMoveVelocity < 0 && vertVelocity == 0)) {
            randomB = RANDOM.nextBoolean() ? -1 : 1;
        }
        lastVerticalMoveVelocity = vertVelocity;

        this.targetBounceY = (float) motionY * bounceIntensity;
        this.targetBounceY += settings().getBustSize() * 1.25F;

        this.targetRotVel = calcRotation(entity, bounceIntensity);
        this.targetRotVel += (float) motionY * bounceIntensity * randomB;

        this.targetBounceX = -calcRotation(entity, bounceIntensity) / 10F;

        double dx = entity.velocityX;
        double dy = entity.velocityY;
        double dz = entity.velocityZ;
        float speedValue = (float) (dx * dx + dy * dy + dz * dz) / 0.2F;
        speedValue = speedValue * speedValue * speedValue;
        if (speedValue < 1.0F) {
            speedValue = 1.0F;
        }
        this.targetBounceY += MathHelper.cos(entity.walkAnimationProgress * 0.6662F + (float) Math.PI)
            * 0.5F * entity.walkAnimationSpeed * 0.5F / speedValue;
    }

    private void tickPose(LivingEntity entity, float bounceIntensity) {
        boolean sneaking = entity.isSneaking();
        boolean sleeping = entity instanceof PlayerEntity && ((PlayerEntity) entity).isSleeping();
        if (sneaking != wasSneaking) {
            this.targetBounceY += bounceIntensity;
            wasSneaking = sneaking;
        }
        if (sleeping != wasSleeping) {
            this.targetBounceY = bounceIntensity;
            wasSleeping = sleeping;
        }
    }

    private void tickVehicle(LivingEntity entity, float bounceIntensity, float breastWeight) {
        Entity vehicle = entity.vehicle;
        if (vehicle == null) {
            return;
        }

        float movement = (float) (vehicle.velocityX * vehicle.velocityX
            + vehicle.velocityY * vehicle.velocityY
            + vehicle.velocityZ * vehicle.velocityZ);
        if (movement <= 0.002F) {
            return;
        }
        int every = Math.max((int) (10 - 2 * movement), 1);
        if (vehicle.ticks % every == 5) {
            this.targetBounceY = (bounceIntensity * MathHelper.clamp(movement * 75F, 0.1F, 1F)) / 4F;
            this.targetBounceY += breastWeight;
        }
    }

    private void tickArmSwing(LivingEntity entity, float bounceIntensity) {
        if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isSleeping()) {
            return;
        }
        if (entity.armSwinging && entity.ticks % 5 == 0) {
            this.targetBounceY += (RANDOM.nextBoolean() ? -0.25F : 0.25F) * bounceIntensity;
            this.targetBounceX = 0.325F * bounceIntensity * -1F;
            this.targetRotVel += -0.2F * bounceIntensity;
        }
    }

    private void finishTick() {
        float percent = settings().getFloppiness();
        float bounceAmount = 0.45F * (1F - percent) + 0.15F;
        bounceAmount = MathHelper.clamp(bounceAmount, 0.15F, 0.6F);
        float delta = 2.25F - bounceAmount;

        float distanceFromMin = Math.abs(bounceVel + 1.5F) * 0.5F;
        float distanceFromMax = Math.abs(bounceVel - 2.65F) * 0.5F;

        if (bounceVel < -0.5F) {
            targetBounceY += distanceFromMin;
        }
        if (bounceVel > 2.5F) {
            targetBounceY -= distanceFromMax;
        }

        targetBounceY = MathHelper.clamp(targetBounceY, -1.5F, 2.5F);
        targetRotVel = MathHelper.clamp(targetRotVel, -25F, 25F);

        this.velocity = lerp(bounceAmount, this.velocity, (this.targetBounceY - this.bounceVel) * delta);
        this.bounceVel += this.velocity * percent * 1.1625F;

        this.velocityX = lerp(bounceAmount, this.velocityX, (this.targetBounceX - this.bounceVelX) * delta);
        this.bounceVelX += this.velocityX * percent;

        this.rotVelocity = lerp(bounceAmount, this.rotVelocity, (this.targetRotVel - this.bounceRotVel) * delta);
        this.bounceRotVel += this.rotVelocity * percent;

        this.bounceRotation = this.bounceRotVel;
        this.positionX = this.bounceVelX;
        this.positionY = this.bounceVel;

        if (this.positionY < -0.5F) {
            this.positionY = -0.5F;
        }
        if (this.positionY > 1.5F) {
            this.positionY = 1.5F;
            this.velocity = 0;
        }
    }

    private static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public float getPositionY(float tickDelta) {
        return lerp(tickDelta, prePositionY, positionY);
    }

    public float getPositionX(float tickDelta) {
        return lerp(tickDelta, prePositionX, positionX);
    }

    public float getBounceRotation(float tickDelta) {
        return lerp(tickDelta, preBounceRotation, bounceRotation);
    }

    public float getBreastSize(float tickDelta) {
        return lerp(tickDelta, preBreastSize, breastSize);
    }
}
