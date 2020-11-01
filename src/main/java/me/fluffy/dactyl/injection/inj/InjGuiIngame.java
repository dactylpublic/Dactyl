package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.event.impl.render.PotionHUDEvent;
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
}
