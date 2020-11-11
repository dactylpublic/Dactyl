package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.action.EventKeyPress;
import me.fluffy.dactyl.module.impl.misc.MCF;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Minecraft.class)
public class InjMinecraft {
    @Inject(method = "middleClickMouse", at = @At(value = "HEAD"))
    private void middleClick(CallbackInfo callbackInfo) {
        if(MCF.INSTANCE.isEnabled()) {
            MCF.INSTANCE.doMiddleClick();
        }
    }


    @Inject(method = "runTickKeyboard", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onRunTickKeyboard(CallbackInfo callbackInfo, int i) {
        if (Keyboard.getEventKeyState()) {
            MinecraftForge.EVENT_BUS.post(new EventKeyPress(i));
        }
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V", shift = At.Shift.BEFORE))
    public void displayCrashReport(CallbackInfo callbackInfo) {
        Dactyl.INSTANCE.save();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdown(CallbackInfo callbackInfo) {
        Dactyl.INSTANCE.save();
    }

}
