package com.feminine.gender.client.gui;

import com.feminine.gender.client.ClientGenderState;
import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.common.PlayerGenderData;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.menu.SurvivalInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;

import net.ornithemc.osl.localization.api.L10n;

public abstract class BaseGenderScreen extends Screen {

    protected static final int DONE_BUTTON = 100;

    protected final Screen parent;

    protected BaseGenderScreen(Screen parent) {
        this.parent = parent;
    }

    protected static String translate(String key) {
        return L10n.get(key);
    }

    protected static String translate(String key, Object... args) {
        return L10n.get(key, args);
    }

    protected static String onOff(boolean value) {
        return value
            ? "\u00a7a" + translate("feminine_gender_mod.label.enabled")
            : "\u00a7c" + translate("feminine_gender_mod.label.disabled");
    }

    protected PlayerGenderData data() {
        return ClientGenderState.local();
    }

    protected GenderSettings settings() {
        PlayerGenderData data = data();
        return data == null ? null : data.settings();
    }

    protected void save() {
        ClientGenderState.save();
    }

    protected void close() {
        this.minecraft.openScreen(parent);
    }

    protected void renderPlayerPreview(int x, int y, int size, int mouseX, int mouseY) {
        if (this.minecraft.player != null) {
            SurvivalInventoryScreen.renderEntity(x, y, size, x - mouseX, y - 50 - mouseY, this.minecraft.player);
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (!button.active) {
            return;
        }
        if (button.id == DONE_BUTTON) {
            close();
        } else {
            onButton(button);
        }
    }

    protected abstract void onButton(ButtonWidget button);

    @Override
    public boolean shouldPauseGame() {
        return false;
    }
}
