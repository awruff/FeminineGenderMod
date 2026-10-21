package com.feminine.gender.client.gui;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.feminine.gender.FeminineGenderMod;
import com.feminine.gender.client.ClientGenderState;
import com.feminine.gender.common.GenderCache;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.common.SyncStatus;
import com.feminine.gender.config.GenderConfig;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class FirstTimeSetupScreen extends BaseGenderScreen {

    private static final int ENABLE_BUTTON = 0;
    private static final int DISABLE_BUTTON = 1;

    private static final int TEXT_WIDTH = 260;

    private ButtonWidget enableButton;
    private ButtonWidget disableButton;

    public FirstTimeSetupScreen(Screen parent) {
        super(parent);
    }

    @Override
    public void init() {
        int x = this.width / 2;
        int y = this.height / 2;

        disableButton = new ButtonWidget(DISABLE_BUTTON, x - 131, y + 74, 128, 20,
            "§c" + translate("feminine_gender_mod.first_time_setup.disable"));
        enableButton = new ButtonWidget(ENABLE_BUTTON, x + 3, y + 74, 128, 20,
            "§a" + translate("feminine_gender_mod.first_time_setup.enable"));

        this.buttons.add(disableButton);
        this.buttons.add(enableButton);
    }

    @Override
    protected void onButton(ButtonWidget button) {
        GenderConfig config = GenderConfig.INSTANCE;
        switch (button.id) {
            case ENABLE_BUTTON:
                config.cloudSync.set(true);
                config.automaticCloudSync.set(true);
                config.firstTimeLoad.set(false);

                enableButton.active = false;
                enableButton.message = "...";
                disableButton.active = false;

                doInitialSync().thenRun(() -> this.minecraft.executeTask(() -> {
                    this.minecraft.openScreen(WardrobeScreen.create(null));
                }));
                break;
            case DISABLE_BUTTON:
                config.cloudSync.set(false);
                config.automaticCloudSync.set(false);
                config.firstTimeLoad.set(false);

                this.minecraft.openScreen(new WardrobeScreen(null));
                break;
            default:
                break;
        }
    }

    private CompletableFuture<Void> doInitialSync() {
        UUID uuid = this.minecraft.player.getUuid();

        GenderCache.removeIf(data -> data.syncStatus == SyncStatus.UNKNOWN);

        final PlayerGenderData data = ClientGenderState.getOrLoad(uuid);
        final boolean hasLocalAppearance = GenderConfig.INSTANCE.hasLocalAppearance();
        return CompletableFuture.runAsync(() -> {
            if (!hasLocalAppearance) {
                try {
                    // wait so the wardrobe opens with the synced data
                    ClientGenderState.loadGenderInfo(data, false, true).join();
                } catch (CompletionException e) {
                    // loadGenderInfo already logged the error
                    return;
                } catch (Exception e) {
                    FeminineGenderMod.LOGGER.error("Failed to perform initial sync from the cloud", e);
                    return;
                }
                this.minecraft.executeTask(() -> {
                    ClientGenderState.save();
                    // this data just came from the cloud, so don't send it straight back
                    data.needsCloudSync = false;
                });
            } else {
                data.needsCloudSync = true;
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        renderBackground();
        int x = this.width / 2;
        int y = this.height / 2;

        drawCenteredString(this.textRenderer, "§n" + translate("feminine_gender_mod.first_time_setup.title"), x, y - 80, 0xFFFFFF);

        List<String> description = this.textRenderer.split(translate("feminine_gender_mod.first_time_setup.description"), TEXT_WIDTH);
        for (int i = 0; i < description.size(); i++) {
            drawCenteredString(this.textRenderer, description.get(i), x, y - 50 + i * 10, 0xFFFFFF);
        }

        drawCenteredString(this.textRenderer, translate("feminine_gender_mod.first_time_setup.notice"), x, y + 60, 0x808080);

        super.render(mouseX, mouseY, tickDelta);

        renderTooltip(enableButton, mouseX, mouseY,
            translate("feminine_gender_mod.first_time_setup.enable.tooltip.line1"),
            translate("feminine_gender_mod.first_time_setup.enable.tooltip.line2"));
    }

    @Override
    public void removed() {
        GenderConfig.INSTANCE.save();
        super.removed();
    }
}
