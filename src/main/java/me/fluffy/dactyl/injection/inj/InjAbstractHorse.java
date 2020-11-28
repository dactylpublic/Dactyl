package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.module.impl.movement.EntityControl;
import net.minecraft.entity.passive.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public class InjAbstractHorse {

    @Inject(method = "canBeSteered", at = @At("HEAD"), cancellable = true)
    public void canBeSteered(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(EntityControl.INSTANCE.isEnabled()) {
            callbackInfoReturnable.cancel();
            callbackInfoReturnable.setReturnValue(Boolean.valueOf(true));
        }
    }

    @Inject(method = "isHorseSaddled", at = @At("HEAD"), cancellable = true)
    public void isHorseSaddled(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(EntityControl.INSTANCE.isEnabled()) {
            callbackInfoReturnable.cancel();
            callbackInfoReturnable.setReturnValue(Boolean.valueOf(true));
        }
    }
}
