package com.feminine.gender.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class GenderSlider extends ButtonWidget {

    public interface Binding {

        float get();

        void set(float value);

        String format(float value);
    }

    private final Binding binding;
    private final float min;
    private final float max;
    private final float step;

    private float normalized;
    private boolean dragging;
    private boolean changed;

    public GenderSlider(int id, int x, int y, int width, int height, float min, float max, float step, Binding binding) {
        super(id, x, y, width, height, "");
        this.binding = binding;
        this.min = min;
        this.max = max;
        this.step = step;
        this.normalized = normalize(binding.get());
        updateMessage();
    }

    private float normalize(float value) {
        if (max <= min) {
            return 0F;
        }
        return MathHelper.clamp((value - min) / (max - min), 0F, 1F);
    }

    private float denormalize(float value) {
        float raw = min + value * (max - min);
        if (step > 0F) {
            raw = Math.round(raw / step) * step;
        }
        return MathHelper.clamp(raw, min, max);
    }

    private void updateMessage() {
        this.message = binding.format(denormalize(normalized));
    }

    public void refresh() {
        this.normalized = normalize(binding.get());
        updateMessage();
    }

    public boolean pollChanged() {
        boolean result = changed;
        changed = false;
        return result;
    }

    private void applyFromMouse(int mouseX) {
        this.normalized = MathHelper.clamp((float) (mouseX - (this.x + 4)) / (this.width - 8), 0F, 1F);
        float value = denormalize(this.normalized);
        if (value != binding.get()) {
            binding.set(value);
            changed = true;
        }
        updateMessage();
    }

    @Override
    protected int getYImage(boolean hovered) {
        return 0;
    }

    @Override
    protected void renderBackground(Minecraft minecraft, int mouseX, int mouseY) {
        if (!this.visible) {
            return;
        }
        if (this.dragging) {
            applyFromMouse(mouseX);
        }
        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        GlStateManager.color4f(1F, 1F, 1F, 1F);
        int handleX = this.x + (int) (this.normalized * (this.width - 8));
        this.drawTexture(handleX, this.y, 0, 66, 4, 20);
        this.drawTexture(handleX + 4, this.y, 196, 66, 4, 20);
    }

    @Override
    public boolean mouseClicked(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.active && super.mouseClicked(minecraft, mouseX, mouseY)) {
            applyFromMouse(mouseX);
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }
}
