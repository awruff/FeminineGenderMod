package com.feminine.gender.client;

import java.util.List;

import com.feminine.gender.client.gui.WardrobeScreen;
import com.feminine.gender.common.GenderCache;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.config.GenderConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.World;

import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer;
import net.ornithemc.osl.keybinds.api.KeybindEvents;
import net.ornithemc.osl.keybinds.api.KeybindRegistry;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;

import org.lwjgl.input.Keyboard;

public class FeminineGenderModClient implements ClientModInitializer {

    private static KeyBinding openWardrobe;

    @Override
    public void initClient() {
        GenderConfig.register();
        ClientGenderNetworking.init();

        KeybindEvents.REGISTER_KEYBINDS.register(() ->
            openWardrobe = KeybindRegistry.register("key.feminine_gender_mod.wardrobe", Keyboard.KEY_G, "key.categories.feminine_gender_mod"));

        MinecraftClientEvents.TICK_END.register(FeminineGenderModClient::tick);
    }

    private static void tick(Minecraft minecraft) {
        if (openWardrobe != null && openWardrobe.consumeClick() && minecraft.screen == null && minecraft.player != null) {
            minecraft.openScreen(new WardrobeScreen(null));
        }

        ClientGenderState.tickSync();

        World world = minecraft.world;
        if (world == null || (minecraft.screen != null && minecraft.screen.shouldPauseGame())) {
            return;
        }

        List<PlayerEntity> players = world.players;
        for (int i = 0; i < players.size(); i++) {
            PlayerEntity player = players.get(i);
            PlayerGenderData data = GenderCache.get(player.getUuid());
            if (data != null && data.settings().getGender().canHaveBreasts()) {
                data.physics().tick(player);
            }
        }
    }
}
