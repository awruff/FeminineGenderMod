package com.feminine.gender.client.render;

import com.feminine.gender.common.GenderArmor;

import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;

public class GenderArmorLayer extends GenderLayer {

    private final BreastBox leftArmor = new BreastBox(64, 32, -4F, 0F, 0F, 4, 5, 3, 0F, BreastUVs.ARMOR_LEFT);
    private final BreastBox rightArmor = new BreastBox(64, 32, 0F, 0F, 0F, 4, 5, 3, 0F, BreastUVs.ARMOR_RIGHT);

    private ArmorItem armorItem;
    private ItemStack armorStack;

    public GenderArmorLayer(PlayerRenderer parent) {
        super(parent);
    }

    @Override
    public void render(ClientPlayerEntity entity, float walkAnimationProgress, float walkAnimationSpeed, float tickDelta,
                       float bob, float yaw, float pitch, float scale) {
        ItemStack chest = entity.getArmor(2);
        if (chest == null || !(chest.getItem() instanceof ArmorItem)) {
            return;
        }
        this.armorStack = chest;
        this.armorItem = (ArmorItem) chest.getItem();
        super.render(entity, walkAnimationProgress, walkAnimationSpeed, tickDelta, bob, yaw, pitch, scale);
    }

    @Override
    public boolean colorsWhenDamaged() {

        return false;
    }

    @Override
    protected boolean isLayerVisible(ClientPlayerEntity entity, GenderArmor armor) {
        return armor.coversBreasts() && !entity.isInvisible();
    }

    @Override
    protected void setupTransformations(ClientPlayerEntity entity, float bob, float scale, BreastSide side) {
        super.setupTransformations(entity, bob, scale, side);
        if (hasJacketLayer) {
            GlStateManager.translatef(0F, 0F, -0.015F);
            GlStateManager.scalef(1.05F, 1.05F, 1.05F);
        }
        GlStateManager.translatef(side.leftOrNegate(0.001F), 0.015F, -0.015F);
        GlStateManager.scalef(1.05F, 1F, 1F);
    }

    @Override
    protected void renderBreast(BreastSide side, float scale) {
        BreastBox box = side.isLeft() ? leftArmor : rightArmor;

        if (armorItem.getTier() == ArmorItem.Tier.CLOTH) {
            int color = armorItem.getColor(armorStack);
            float r = (color >> 16 & 0xFF) / 255F;
            float g = (color >> 8 & 0xFF) / 255F;
            float b = (color & 0xFF) / 255F;
            parent.bindTexture(texture(null));
            GlStateManager.color4f(r, g, b, 1F);
            box.render(scale);

            parent.bindTexture(texture("overlay"));
            GlStateManager.color4f(1F, 1F, 1F, 1F);
            box.render(scale);
        } else {
            parent.bindTexture(texture(null));
            GlStateManager.color4f(1F, 1F, 1F, 1F);
            box.render(scale);
        }
    }

    private Identifier texture(String type) {
        return new Identifier(String.format("textures/models/armor/%s_layer_1%s.png",
            armorItem.getTier().getKey(), type == null ? "" : "_" + type));
    }
}
