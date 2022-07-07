package me.chloe.dactyl.injection.inj;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.event.impl.action.EventKeyPress;
import me.chloe.dactyl.module.impl.misc.MCF;
import me.chloe.dactyl.module.impl.player.MultiTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = { "sendClickBlockToController" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    private boolean isHandActiveWrapper(final EntityPlayerSP playerSP) {
        return !MultiTask.INSTANCE.isEnabled() && playerSP.isHandActive();
    }

    @Redirect(method = { "rightClickMouse" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z", ordinal = 0), require = 1)
    private boolean isHittingBlockHook(final PlayerControllerMP playerControllerMP) {
        return !MultiTask.INSTANCE.isEnabled() && playerControllerMP.getIsHittingBlock();
    }

}
