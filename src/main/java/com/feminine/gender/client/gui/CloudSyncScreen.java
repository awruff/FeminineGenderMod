package com.feminine.gender.client.gui;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.feminine.gender.FeminineGenderMod;
import com.feminine.gender.client.cloud.CloudSync;
import com.feminine.gender.client.cloud.SyncLog;
import com.feminine.gender.client.cloud.SyncingTooFrequentlyException;
import com.feminine.gender.common.PlayerGenderData;
import com.feminine.gender.config.GenderConfig;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class CloudSyncScreen extends BaseGenderScreen {

    private static final int STATUS_BUTTON = 0;
    private static final int AUTOMATIC_BUTTON = 1;
    private static final int SYNC_BUTTON = 2;
    private static final int DELETE_BUTTON = 3;

    private static final int MAX_LOG_LINES = 6;

    private ButtonWidget statusButton;
    private ButtonWidget automaticButton;
    private ButtonWidget syncButton;
    private ButtonWidget deleteButton;

    public CloudSyncScreen(Screen parent) {
        super(parent);
    }

    private int top() {
        return this.height / 2 - 80;
    }

    @Override
    public void init() {
        int x = this.width / 2 - 100;
        int top = top();

        statusButton = new ButtonWidget(STATUS_BUTTON, x, top + 14, 200, 20, statusLabel());
        automaticButton = new ButtonWidget(AUTOMATIC_BUTTON, x, top + 38, 200, 20, automaticLabel());
        automaticButton.active = CloudSync.isEnabled();
        syncButton = new ButtonWidget(SYNC_BUTTON, x, top + 62, 200, 20, translate("feminine_gender_mod.cloud.sync"));
        syncButton.visible = CloudSync.isEnabled();
        deleteButton = new ButtonWidget(DELETE_BUTTON, x, top + 62, 200, 20, "§c" + translate("feminine_gender_mod.cloud.delete"));
        deleteButton.visible = !CloudSync.isEnabled();

        this.buttons.add(statusButton);
        this.buttons.add(automaticButton);
        this.buttons.add(syncButton);
        this.buttons.add(deleteButton);
        this.buttons.add(new ButtonWidget(DONE_BUTTON, x, top + 160, 200, 20, translate("gui.done")));
    }

    private static String statusLabel() {
        return translate("feminine_gender_mod.cloud.status", onOff(CloudSync.isEnabled()));
    }

    private static String automaticLabel() {
        String value = CloudSync.isEnabled()
            ? onOff(GenderConfig.INSTANCE.automaticCloudSync())
            : translate("feminine_gender_mod.label.off");
        return translate("feminine_gender_mod.cloud.automatic", value);
    }

    @Override
    protected void onButton(ButtonWidget button) {
        GenderConfig config = GenderConfig.INSTANCE;
        switch (button.id) {
            case STATUS_BUTTON:
                config.cloudSync.set(!config.cloudSyncEnabled());
                boolean available = CloudSync.isAvailable();
                boolean enabled = config.cloudSyncEnabled();
                statusButton.message = statusLabel();
                automaticButton.active = enabled;
                syncButton.visible = enabled && available;
                deleteButton.visible = !enabled && available;
                automaticButton.message = automaticLabel();
                break;
            case AUTOMATIC_BUTTON:
                config.automaticCloudSync.set(!config.automaticCloudSync());
                automaticButton.message = automaticLabel();
                break;
            case SYNC_BUTTON:
                sync(button);
                break;
            case DELETE_BUTTON:
                delete(button);
                break;
            default:
                break;
        }
    }

    private void sync(final ButtonWidget button) {
        final PlayerGenderData data = data();
        if (data == null) {
            return;
        }
        button.active = false;
        button.message = translate("feminine_gender_mod.cloud.syncing");
        CompletableFuture.runAsync(() -> {
            String result;
            try {
                CloudSync.sync(data).join();
                result = translate("feminine_gender_mod.cloud.syncing.success");
            } catch (Exception e) {
                Throwable actualException = CloudSync.unwrap(e);
                if (actualException instanceof SyncingTooFrequentlyException) {
                    FeminineGenderMod.LOGGER.warn("Failed to sync settings as we've already synced too recently");
                    SyncLog.add("feminine_gender_mod.sync_log.sync_too_frequently");
                } else {
                    FeminineGenderMod.LOGGER.error("Failed to sync settings", actualException);
                }
                result = translate("feminine_gender_mod.cloud.syncing.fail");
            }
            setMessageLater(button, result);
        });
    }

    private void delete(final ButtonWidget button) {
        final PlayerGenderData data = data();
        if (data == null) {
            return;
        }
        button.active = false;
        CompletableFuture.runAsync(() -> {
            String result;
            try {
                CloudSync.deleteProfile(data).join();
                result = translate("feminine_gender_mod.cloud.deleted");
            } catch (Exception e) {
                FeminineGenderMod.LOGGER.error("Failed to delete cloud sync profile", CloudSync.unwrap(e));
                result = translate("feminine_gender_mod.cloud.delete_failed");
            }
            setMessageLater(button, result);
        });
    }

    private void setMessageLater(final ButtonWidget button, final String message) {
        this.minecraft.executeTask(() -> {
            button.message = message;
        });
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        renderBackground();
        int x = this.width / 2 - 100;
        int top = top();
        drawCenteredString(this.textRenderer, translate("feminine_gender_mod.cloud_settings"), this.width / 2, top, 0xFFFFFF);
        drawString(this.textRenderer, translate("feminine_gender_mod.cloud.status_log"), x, top + 90, 0xAAAAAA);

        List<SyncLog.Entry> entries = SyncLog.entries();
        for (int i = 0; i < entries.size() && i < MAX_LOG_LINES; i++) {
            SyncLog.Entry entry = entries.get(entries.size() - 1 - i);
            drawString(this.textRenderer, translate(entry.translationKey()), x + 2, top + 102 + (MAX_LOG_LINES - 1 - i) * 10, entry.color());
        }

        super.render(mouseX, mouseY, tickDelta);

        renderTooltip(automaticButton, mouseX, mouseY,
            translate("feminine_gender_mod.cloud.automatic.tooltip.line1"),
            translate("feminine_gender_mod.cloud.automatic.tooltip.line2"));
    }

    @Override
    public void removed() {
        GenderConfig.INSTANCE.save();
        super.removed();
    }
}
