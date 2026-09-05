package com.feminine.gender.client.render;

import com.feminine.gender.FeminineGenderMod;
import com.feminine.gender.common.GenderArmor;
import com.feminine.gender.common.GenderCache;
import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.config.GenderConfig;
import com.feminine.gender.physics.BreastPhysics;

import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.entity.layer.EntityRenderLayer;
import net.minecraft.client.render.model.PlayerModelPart;
import net.minecraft.client.render.model.entity.PlayerModel;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class GenderLayer implements EntityRenderLayer<ClientPlayerEntity> {

    private static final BreastSide[] SIDES = BreastSide.values();

    protected final PlayerRenderer parent;

    private final BreastBox leftBreast = new BreastBox(64, 64, -4F, 0F, 0F, 4, 5, 3, 0F, BreastUVs.SKIN_LEFT);
    private final BreastBox rightBreast = new BreastBox(64, 64, 0F, 0F, 0F, 4, 5, 3, 0F, BreastUVs.SKIN_RIGHT);
    private final BreastBox leftOverlay = new BreastBox(64, 64, -4F, 0F, 0F, 4, 5, 3, 0F, BreastUVs.OVERLAY_LEFT);
    private final BreastBox rightOverlay = new BreastBox(64, 64, 0F, 0F, 0F, 4, 5, 3, 0F, BreastUVs.OVERLAY_RIGHT);

    protected boolean isChestplateOccupied;
    protected boolean bounceEnabled;
    protected boolean breathingAnimation;
    protected boolean isUniboob;
    protected boolean hasJacketLayer;
    protected float breastOffsetX, breastOffsetY, breastOffsetZ;
    protected float lPhysPositionX, lPhysPositionY, lPhysBounceRotation;
    protected float rPhysPositionX, rPhysPositionY, rPhysBounceRotation;
    protected float breastSize, zOffset, outwardAngle;

    public GenderLayer(PlayerRenderer parent) {
        this.parent = parent;
    }

    @Override
    public void render(ClientPlayerEntity entity, float walkAnimationProgress, float walkAnimationSpeed, float tickDelta,
                       float bob, float yaw, float pitch, float scale) {
        PlayerGenderData data = GenderCache.get(entity.getUuid());
        if (data == null) {
            return;
        }
        try {
            if (!setupRender(entity, data, tickDelta)) {
                return;
            }
            parent.bindTexture(entity.getSkinTextureLocation());
            renderSides(entity, data, bob, scale);
        } catch (Exception e) {
            FeminineGenderMod.LOGGER.error("Failed to render breast layer", e);
        }
    }

    @Override
    public boolean colorsWhenDamaged() {
        return true;
    }

    protected boolean setupRender(ClientPlayerEntity entity, PlayerGenderData data, float tickDelta) {
        if (GenderConfig.INSTANCE.disableRendering()) {
            return false;
        }

        GenderSettings settings = data.settings();
        if (!settings.getGender().canHaveBreasts()) {
            return false;
        }

        boolean armorPhysicsOverride = GenderConfig.INSTANCE.armorPhysicsOverride();
        GenderArmor armor = GenderArmor.forStack(entity.getArmor(2));
        isChestplateOccupied = armor.coversBreasts() && !armorPhysicsOverride;
        if (!settings.showInArmor() && isChestplateOccupied) {
            return false;
        }

        if (!isLayerVisible(entity, armor)) {
            return false;
        }

        breastOffsetX = round(settings.getXOffset(), 1);
        breastOffsetY = -round(settings.getYOffset(), 1);
        breastOffsetZ = -round(settings.getZOffset(), 1);

        isUniboob = settings.isUniboob();
        hasJacketLayer = entity.isModelPartVisible(PlayerModelPart.JACKET);

        BreastPhysics left = data.physics().left();
        float bSize = left.getBreastSize(tickDelta);
        outwardAngle = Math.min(Math.round(settings.getCleavage() * 100F), 10);

        lPhysPositionY = left.getPositionY(tickDelta);
        lPhysPositionX = left.getPositionX(tickDelta);
        lPhysBounceRotation = left.getBounceRotation(tickDelta);
        if (isUniboob) {
            rPhysPositionY = lPhysPositionY;
            rPhysPositionX = lPhysPositionX;
            rPhysBounceRotation = lPhysBounceRotation;
        } else {
            BreastPhysics right = data.physics().right();
            rPhysPositionY = right.getPositionY(tickDelta);
            rPhysPositionX = right.getPositionX(tickDelta);
            rPhysBounceRotation = right.getBounceRotation(tickDelta);
        }

        breastSize = Math.min(bSize * 1.5F, 0.7F);
        if (bSize > 0.7F) {
            breastSize = bSize;
        }
        if (breastSize < 0.02F) {
            return false;
        }

        zOffset = 0.0625F - (bSize * 0.0625F);
        breastSize += 0.5F * Math.abs(bSize - 0.7F) * 2F;

        float resistance = MathHelper.clamp(armor.physicsResistance(), 0F, 1F);
        breathingAnimation = armorPhysicsOverride || resistance <= 0.5F;
        bounceEnabled = settings.hasPhysics() && (!isChestplateOccupied || resistance < 1);
        return true;
    }

    protected boolean isLayerVisible(ClientPlayerEntity entity, GenderArmor armor) {
        return !entity.isInvisible();
    }

    protected void renderSides(ClientPlayerEntity entity, PlayerGenderData data, float bob, float scale) {
        for (BreastSide side : SIDES) {
            GlStateManager.pushMatrix();
            try {
                setupTransformations(entity, bob, scale, side);
                renderBreast(side, scale);
            } finally {
                GlStateManager.popMatrix();
            }
        }
    }

    protected void setupTransformations(ClientPlayerEntity entity, float bob, float scale, BreastSide side) {
        PlayerModel model = parent.getModel();

        if (model.isBaby) {
            GlStateManager.scalef(0.5F, 0.5F, 0.5F);
            GlStateManager.translatef(0F, 24F * scale, 0F);
        } else if (entity.isSneaking()) {
            GlStateManager.translatef(0F, 0.2F, 0F);
        }

        model.body.transform(scale);

        if (bounceEnabled) {
            GlStateManager.translatef(side.forSide(lPhysPositionX, rPhysPositionX) / 32F,
                side.forSide(lPhysPositionY, rPhysPositionY) / 32F, 0F);
        }

        GlStateManager.translatef(side.leftOrNegate(breastOffsetX) * 0.0625F,
            0.05625F + (breastOffsetY * 0.0625F),
            zOffset - 0.0625F * 2F + (breastOffsetZ * 0.0425F));

        if (!isUniboob) {
            GlStateManager.translatef(side.leftOrNegate(-0.0625F * 2F), 0F, 0F);
        }
        if (bounceEnabled) {
            GlStateManager.rotatef(side.forSide(lPhysBounceRotation, rPhysBounceRotation), 0F, 1F, 0F);
        }
        if (!isUniboob) {
            GlStateManager.translatef(side.leftOrNegate(0.0625F * 2F), 0F, 0F);
        }

        float rotation = breastSize;
        if (bounceEnabled) {
            GlStateManager.translatef(0F, -0.035F * breastSize, 0F);
            rotation -= side.forSide(lPhysPositionY, rPhysPositionY) / 12F;
        }
        rotation = Math.min(rotation, breastSize + 0.2F);
        rotation = Math.min(rotation, 1F);

        if (isChestplateOccupied) {
            GlStateManager.translatef(0F, 0F, 0.01F);
        }

        GlStateManager.rotatef(side.leftOrNegate(outwardAngle), 0F, 1F, 0F);
        float xRotation = -35F * rotation;
        if (breathingAnimation) {
            xRotation += -MathHelper.cos(bob * 0.09F) * 0.45F + 0.45F;
        }
        GlStateManager.rotatef(xRotation, 1F, 0F, 0F);

        GlStateManager.scalef(0.9995F, 1F, 1F);
    }

    protected void renderBreast(BreastSide side, float scale) {
        (side.isLeft() ? leftBreast : rightBreast).render(scale);

        if (hasJacketLayer) {
            GlStateManager.translatef(0F, 0F, -0.015F);
            GlStateManager.scalef(1.05F, 1.05F, 1.05F);
            (side.isLeft() ? leftOverlay : rightOverlay).render(scale);
        }
    }

    private static float round(float value, int places) {
        float factor = (float) Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }
}
