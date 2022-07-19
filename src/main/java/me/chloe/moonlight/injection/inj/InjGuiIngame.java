package me.chloe.moonlight.injection.inj;

import me.chloe.moonlight.event.impl.render.PotionHUDEvent;
import me.chloe.moonlight.module.impl.render.NoRender;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class InjGuiIngame {
    @Inject(method = "renderPotionEffects", at = @At("HEAD"), cancellable = true)
    public void renderPotions(ScaledResolution res, CallbackInfo info) {
        PotionHUDEvent event = new PotionHUDEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if(event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "renderPortal", at = @At("HEAD"), cancellable = true)
    protected void renderPortalHook(final float n, final ScaledResolution scaledResolution, final CallbackInfo info) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.portalAnim.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = { "renderPumpkinOverlay" }, at = { @At("HEAD") }, cancellable = true)
    protected void renderPumpkinOverlayHook(final ScaledResolution scaledRes, final CallbackInfo info) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.pumpkin.getValue()) {
            info.cancel();
        }
    }
}
