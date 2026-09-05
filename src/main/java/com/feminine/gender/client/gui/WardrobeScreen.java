package com.feminine.gender.client.gui;

import com.feminine.gender.common.Gender;
import com.feminine.gender.common.GenderSettings;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class WardrobeScreen extends BaseGenderScreen {

    private static final int GENDER_BUTTON = 0;
    private static final int CUSTOMIZE_BUTTON = 1;

    private ButtonWidget genderButton;
    private ButtonWidget customizeButton;

    public WardrobeScreen(Screen parent) {
        super(parent);
    }

    @Override
    public void init() {
        GenderSettings settings = settings();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        genderButton = new ButtonWidget(GENDER_BUTTON, centerX - 100, centerY + 10, 200, 20, genderLabel(settings));
        customizeButton = new ButtonWidget(CUSTOMIZE_BUTTON, centerX - 100, centerY + 34, 200, 20,
            translate("feminine_gender_mod.appearance_settings.title") + "...");
        customizeButton.active = settings != null && settings.getGender().canHaveBreasts();

        this.buttons.add(genderButton);
        this.buttons.add(customizeButton);
        this.buttons.add(new ButtonWidget(DONE_BUTTON, centerX - 100, centerY + 62, 200, 20, translate("gui.done")));
    }

    private String genderLabel(GenderSettings settings) {
        if (settings == null) {
            return "";
        }
        Gender gender = settings.getGender();
        return translate("feminine_gender_mod.label.gender") + ": " + gender.getColor() + translate(gender.getTranslationKey());
    }

    @Override
    protected void onButton(ButtonWidget button) {
        GenderSettings settings = settings();
        if (settings == null) {
            return;
        }
        switch (button.id) {
            case GENDER_BUTTON:
                settings.setGender(settings.getGender().next());
                save();
                genderButton.message = genderLabel(settings);
                customizeButton.active = settings.getGender().canHaveBreasts();
                break;
            case CUSTOMIZE_BUTTON:
                this.minecraft.openScreen(new CustomizationScreen(this));
                break;
            default:
                break;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        renderBackground();
        drawCenteredString(this.textRenderer, translate("feminine_gender_mod.wardrobe.title"), this.width / 2, 20, 0xFFFFFF);
        renderPlayerPreview(this.width / 2, this.height / 2 - 10, 40, mouseX, mouseY);
        super.render(mouseX, mouseY, tickDelta);
    }
}
