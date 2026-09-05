package com.feminine.gender.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.feminine.gender.common.GenderCache;
import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.config.GenderConfig;

import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Unique
    private static final byte DAMAGED_EVENT = 2;

    @Unique
    private static final String FEMALE_HURT_SOUND = "feminine_gender_mod:female_hurt";

    @Inject(method = "doEvent", at = @At("HEAD"))
    private void feminineGenderMod$playFemaleHurtSound(byte event, CallbackInfo ci) {
        if (event != DAMAGED_EVENT || !(((Object) this) instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!player.world.isClient || GenderConfig.INSTANCE.disableSoundReplacement()) {
            return;
        }
        PlayerGenderData data = GenderCache.get(player.getUuid());
        if (data == null) {
            return;
        }
        GenderSettings settings = data.settings();
        if (!settings.hasHurtSounds() || !settings.getGender().canHaveBreasts()) {
            return;
        }

        player.world.playSound(player.x, player.y, player.z, FEMALE_HURT_SOUND,
            1F, settings.getVoicePitch(), false);
    }
}
