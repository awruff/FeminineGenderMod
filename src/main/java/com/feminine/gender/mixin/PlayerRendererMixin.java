package com.feminine.gender.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.feminine.gender.client.render.GenderArmorLayer;
import com.feminine.gender.client.render.GenderLayer;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerRenderer;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "<init>(Lnet/minecraft/client/render/entity/EntityRenderDispatcher;Z)V", at = @At("TAIL"))
    private void feminineGenderMod$addBreastLayers(EntityRenderDispatcher dispatcher, boolean thinArms, CallbackInfo ci) {
        PlayerRenderer self = (PlayerRenderer) (Object) this;
        LivingEntityRendererInvoker invoker = (LivingEntityRendererInvoker) this;
        invoker.feminineGenderMod$addLayer(new GenderLayer(self));
        invoker.feminineGenderMod$addLayer(new GenderArmorLayer(self));
    }
}
