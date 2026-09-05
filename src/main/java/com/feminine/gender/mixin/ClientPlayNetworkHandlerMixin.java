package com.feminine.gender.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.feminine.gender.common.GenderCache;
import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.config.GenderConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.SoundEventS2CPacket;

@Mixin(net.minecraft.client.network.handler.ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    private static final String VANILLA_HURT_SOUND = "game.player.hurt";
    private static final String FEMALE_HURT_SOUND = "feminine_gender_mod:female_hurt";

    private static final double MATCH_DISTANCE_SQ = 0.5 * 0.5;

    @Inject(method = "handleSoundEvent", at = @At("HEAD"), cancellable = true)
    private void feminineGenderMod$replaceHurtSound(SoundEventS2CPacket packet, CallbackInfo ci) {
        if (!VANILLA_HURT_SOUND.equals(packet.getSound()) || GenderConfig.INSTANCE.disableSoundReplacement()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientWorld world = minecraft.world;
        if (world == null) {
            return;
        }

        PlayerEntity source = feminineGenderMod$findPlayer(world, packet.getX(), packet.getY(), packet.getZ());
        if (source == null) {
            return;
        }
        PlayerGenderData data = GenderCache.get(source.getUuid());
        if (data == null) {
            return;
        }
        GenderSettings settings = data.settings();
        if (!settings.hasHurtSounds() || !settings.getGender().canHaveBreasts()) {
            return;
        }

        world.playSound(packet.getX(), packet.getY(), packet.getZ(), FEMALE_HURT_SOUND,
            packet.getVolume(), settings.getVoicePitch(), false);
        ci.cancel();
    }

    private PlayerEntity feminineGenderMod$findPlayer(ClientWorld world, double x, double y, double z) {
        List<PlayerEntity> players = world.players;
        for (int i = 0; i < players.size(); i++) {
            PlayerEntity player = players.get(i);
            double dx = player.x - x;
            double dy = player.y - y;
            double dz = player.z - z;
            if (dx * dx + dy * dy + dz * dz <= MATCH_DISTANCE_SQ) {
                return player;
            }
        }
        return null;
    }
}
