package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.module.impl.movement.AntiLevitation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public class InjEntityLivingBase {
    @Inject(method = "isPotionActive", at = @At("HEAD"), cancellable = true)
    public void isPotionActive(Potion potion, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if(AntiLevitation.INSTANCE == null) {
            return;
        }
        if(potion == MobEffects.LEVITATION) {
            if(AntiLevitation.INSTANCE.isEnabled()) {
                callbackInfoReturnable.setReturnValue(false);
            }
        }
    }
}
