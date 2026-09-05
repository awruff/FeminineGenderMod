package com.feminine.gender.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.feminine.gender.common.GenderSettings;
import com.feminine.gender.config.GenderConfig;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class CustomizationScreen extends BaseGenderScreen {

    private enum Tab {
        CUSTOMIZATION("feminine_gender_mod.breast_customization.tab_customization"),
        PHYSICS("feminine_gender_mod.breast_customization.tab_physics"),
        MISC("feminine_gender_mod.breast_customization.tab_miscellaneous");

        final String key;

        Tab(String key) {
            this.key = key;
        }
    }

    private static final int TAB_CUSTOMIZATION = 10;
    private static final int TAB_PHYSICS = 11;
    private static final int TAB_MISC = 12;

    private static final int TOGGLE_PHYSICS = 20;
    private static final int TOGGLE_UNIBOOB = 21;
    private static final int TOGGLE_ARMOR_PHYSICS = 22;
    private static final int TOGGLE_HURT_SOUNDS = 23;
    private static final int TOGGLE_SHOW_IN_ARMOR = 24;

    private static final int FULL_WIDTH = 200;
    private static final int HALF_WIDTH = FULL_WIDTH / 2 - 2;

    private final List<GenderSlider> sliders = new ArrayList<GenderSlider>();

    private Tab currentTab = Tab.CUSTOMIZATION;

    public CustomizationScreen(Screen parent) {
        super(parent);
    }

    @Override
    public void init() {
        sliders.clear();

        int centerX = this.width / 2;
        int top = this.height / 2 - 60;

        this.buttons.add(tabButton(TAB_CUSTOMIZATION, Tab.CUSTOMIZATION, centerX - 100, top));
        this.buttons.add(tabButton(TAB_PHYSICS, Tab.PHYSICS, centerX - 33, top));
        this.buttons.add(tabButton(TAB_MISC, Tab.MISC, centerX + 34, top));

        final GenderSettings settings = settings();
        if (settings != null) {
            int y = top + 24;
            switch (currentTab) {
                case CUSTOMIZATION:
                    initCustomizationTab(settings, centerX, y);
                    break;
                case PHYSICS:
                    initPhysicsTab(settings, centerX, y);
                    break;
                default:
                    initMiscTab(settings, centerX, y);
                    break;
            }
        }

        this.buttons.add(new ButtonWidget(DONE_BUTTON, centerX - 100, this.height / 2 + 70, FULL_WIDTH, 20, translate("gui.done")));
    }

    private ButtonWidget tabButton(int id, Tab tab, int x, int y) {
        ButtonWidget button = new ButtonWidget(id, x, y, 66, 20, translate(tab.key));
        button.active = currentTab != tab;
        return button;
    }

    private void addSlider(GenderSlider slider) {
        sliders.add(slider);
        this.buttons.add(slider);
    }

    private void initCustomizationTab(final GenderSettings settings, int centerX, int y) {
        addSlider(new GenderSlider(0, centerX - 100, y, FULL_WIDTH, 20,
            GenderSettings.MIN_BUST_SIZE, GenderSettings.MAX_BUST_SIZE, 0.01F, new GenderSlider.Binding() {
                @Override
                public float get() {
                    return settings.getBustSize();
                }

                @Override
                public void set(float value) {
                    settings.setBustSize(value);
                }

                @Override
                public String format(float value) {
                    return translate("feminine_gender_mod.wardrobe.slider.breast_size", Integer.valueOf(Math.round(value * 1.25F * 100)));
                }
            }));

        addSlider(new GenderSlider(1, centerX - 100, y + 24, HALF_WIDTH, 20, -1F, 1F, 0.01F, new GenderSlider.Binding() {
            @Override
            public float get() {
                return settings.getXOffset();
            }

            @Override
            public void set(float value) {
                settings.setXOffset(value);
            }

            @Override
            public String format(float value) {
                return translate("feminine_gender_mod.wardrobe.slider.separation", Integer.valueOf(Math.round(value * 10)));
            }
        }));

        addSlider(new GenderSlider(2, centerX - 100 + HALF_WIDTH + 4, y + 24, HALF_WIDTH, 20, -1F, 1F, 0.01F, new GenderSlider.Binding() {
            @Override
            public float get() {
                return settings.getYOffset();
            }

            @Override
            public void set(float value) {
                settings.setYOffset(value);
            }

            @Override
            public String format(float value) {
                return translate("feminine_gender_mod.wardrobe.slider.height", Integer.valueOf(Math.round(value * 10)));
            }
        }));

        addSlider(new GenderSlider(3, centerX - 100, y + 48, HALF_WIDTH, 20, -1F, 0F, 0.1F, new GenderSlider.Binding() {
            @Override
            public float get() {
                return settings.getZOffset();
            }

            @Override
            public void set(float value) {
                settings.setZOffset(value);
            }

            @Override
            public String format(float value) {
                return translate("feminine_gender_mod.wardrobe.slider.depth", Integer.valueOf(Math.round(value * 10)));
            }
        }));

        addSlider(new GenderSlider(4, centerX - 100 + HALF_WIDTH + 4, y + 48, HALF_WIDTH, 20,
            GenderSettings.MIN_CLEAVAGE, GenderSettings.MAX_CLEAVAGE, 0.01F, new GenderSlider.Binding() {
                @Override
                public float get() {
                    return settings.getCleavage();
                }

                @Override
                public void set(float value) {
                    settings.setCleavage(value);
                }

                @Override
                public String format(float value) {
                    return translate("feminine_gender_mod.wardrobe.slider.rotation", Integer.valueOf(Math.round(value * 100)));
                }
            }));
    }

    private void initPhysicsTab(final GenderSettings settings, int centerX, int y) {
        boolean physics = settings.hasPhysics();

        this.buttons.add(new ButtonWidget(TOGGLE_PHYSICS, centerX - 100, y, FULL_WIDTH, 20,
            translate("feminine_gender_mod.char_settings.physics", onOff(physics))));

        ButtonWidget uniboob = new ButtonWidget(TOGGLE_UNIBOOB, centerX - 100, y + 24, FULL_WIDTH, 20,
            translate("feminine_gender_mod.breast_customization.dual_physics", settings.isUniboob() ? translate("gui.no") : translate("gui.yes")));
        uniboob.active = physics;
        this.buttons.add(uniboob);

        GenderSlider bounce = new GenderSlider(5, centerX - 100, y + 48, HALF_WIDTH, 20,
            GenderSettings.MIN_BOUNCE, GenderSettings.MAX_BOUNCE, 0.005F, new GenderSlider.Binding() {
                @Override
                public float get() {
                    return settings.getBounceMultiplier();
                }

                @Override
                public void set(float value) {
                    settings.setBounceMultiplier(value);
                }

                @Override
                public String format(float value) {
                    return translate("feminine_gender_mod.slider.bounce", Integer.valueOf(Math.round(3 * value * 100)));
                }
            });
        bounce.active = physics;
        addSlider(bounce);

        GenderSlider floppy = new GenderSlider(6, centerX - 100 + HALF_WIDTH + 4, y + 48, HALF_WIDTH, 20,
            GenderSettings.MIN_FLOPPINESS, GenderSettings.MAX_FLOPPINESS, 0.01F, new GenderSlider.Binding() {
                @Override
                public float get() {
                    return settings.getFloppiness();
                }

                @Override
                public void set(float value) {
                    settings.setFloppiness(value);
                }

                @Override
                public String format(float value) {
                    return translate("feminine_gender_mod.slider.floppy", Integer.valueOf(Math.round(value * 100)));
                }
            });
        floppy.active = physics;
        addSlider(floppy);

        this.buttons.add(new ButtonWidget(TOGGLE_ARMOR_PHYSICS, centerX - 100, y + 72, FULL_WIDTH, 20,
            translate("feminine_gender_mod.char_settings.override_armor_physics", onOff(GenderConfig.INSTANCE.armorPhysicsOverride()))));
    }

    private void initMiscTab(final GenderSettings settings, int centerX, int y) {
        this.buttons.add(new ButtonWidget(TOGGLE_HURT_SOUNDS, centerX - 100, y, FULL_WIDTH, 20,
            translate("feminine_gender_mod.char_settings.hurt_sounds", onOff(settings.hasHurtSounds()))));

        GenderSlider pitch = new GenderSlider(7, centerX - 100, y + 24, FULL_WIDTH, 20,
            GenderSettings.MIN_VOICE_PITCH, GenderSettings.MAX_VOICE_PITCH, 0.01F, new GenderSlider.Binding() {
                @Override
                public float get() {
                    return settings.getVoicePitch();
                }

                @Override
                public void set(float value) {
                    settings.setVoicePitch(value);
                }

                @Override
                public String format(float value) {
                    return translate("feminine_gender_mod.slider.voice_pitch", Integer.valueOf(Math.round(value * 100)));
                }
            });
        pitch.active = settings.hasHurtSounds();
        addSlider(pitch);

        this.buttons.add(new ButtonWidget(TOGGLE_SHOW_IN_ARMOR, centerX - 100, y + 48, FULL_WIDTH, 20,
            translate("feminine_gender_mod.char_settings.hide_in_armor", onOff(!settings.showInArmor()))));
    }

    @Override
    protected void onButton(ButtonWidget button) {
        GenderSettings settings = settings();
        switch (button.id) {
            case TAB_CUSTOMIZATION:
                switchTab(Tab.CUSTOMIZATION);
                return;
            case TAB_PHYSICS:
                switchTab(Tab.PHYSICS);
                return;
            case TAB_MISC:
                switchTab(Tab.MISC);
                return;
            default:
                break;
        }
        if (settings == null) {
            return;
        }
        switch (button.id) {
            case TOGGLE_PHYSICS:
                settings.setPhysics(!settings.hasPhysics());
                save();
                rebuild();
                break;
            case TOGGLE_UNIBOOB:
                settings.setUniboob(!settings.isUniboob());
                save();
                rebuild();
                break;
            case TOGGLE_ARMOR_PHYSICS:
                GenderConfig.INSTANCE.armorPhysicsOverride.set(Boolean.valueOf(!GenderConfig.INSTANCE.armorPhysicsOverride()));
                GenderConfig.INSTANCE.save();
                rebuild();
                break;
            case TOGGLE_HURT_SOUNDS:
                settings.setHurtSounds(!settings.hasHurtSounds());
                save();
                rebuild();
                break;
            case TOGGLE_SHOW_IN_ARMOR:
                settings.setShowInArmor(!settings.showInArmor());
                save();
                rebuild();
                break;
            default:
                break;
        }
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
        rebuild();
    }

    private void rebuild() {
        this.buttons.clear();
        init();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        boolean changed = false;
        for (int i = 0; i < sliders.size(); i++) {
            changed |= sliders.get(i).pollChanged();
        }
        if (changed) {
            save();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        renderBackground();
        drawCenteredString(this.textRenderer, translate("feminine_gender_mod.appearance_settings.title"), this.width / 2, 20, 0xFFFFFF);
        renderPlayerPreview(this.width / 2 - 130, this.height / 2 + 40, 45, mouseX, mouseY);
        super.render(mouseX, mouseY, tickDelta);
    }
}
