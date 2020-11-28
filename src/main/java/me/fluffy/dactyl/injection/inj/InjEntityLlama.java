package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.module.impl.movement.EntityControl;
import net.minecraft.entity.passive.EntityLlama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLlama.class)
public class InjEntityLlama {
    @Inject(method = "canBeSteered", at = @At("HEAD"), cancellable = true)
    public void canBeSteered(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(EntityControl.INSTANCE.isEnabled()) {
            callbackInfoReturnable.cancel();
            callbackInfoReturnable.setReturnValue(Boolean.valueOf(true));
        }
    }
}
