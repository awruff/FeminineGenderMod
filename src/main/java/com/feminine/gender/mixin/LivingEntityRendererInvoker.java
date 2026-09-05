package com.feminine.gender.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.layer.EntityRenderLayer;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {

    @SuppressWarnings("rawtypes")
    @Invoker("addLayer")
    boolean feminineGenderMod$addLayer(EntityRenderLayer layer);
}
